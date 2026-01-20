package com.payflow.global.gemini;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final WebClient webClient;
    
    @Value("${gemini.api.key}")
    private String apiKey;

    public GeminiClient(@Value("${gemini.api.url}") String apiUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .build();
    }

    public boolean isConfigured() {
        return apiKey != null
            && !apiKey.isBlank()
            && !apiKey.equalsIgnoreCase("your-gemini-api-key-here");
    }

    /**
     * Gemini API 호출
     */
    public String callGemini(String prompt) {
        try {
            // Gemini API 요청 형식
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            Mono<Map> response = webClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class);

            Map<String, Object> result = response.block();
            
            if (result != null && result.containsKey("candidates")) {
                List<Map> candidates = (List<Map>) result.get("candidates");
                if (!candidates.isEmpty()) {
                    Map candidate = candidates.get(0);
                    Map content = (Map) candidate.get("content");
                    List<Map> parts = (List<Map>) content.get("parts");
                    if (!parts.isEmpty()) {
                        String text = (String) parts.get(0).get("text");
                        
                        log.info("🤖 Gemini 응답 성공");
                        return text;
                    }
                }
            }
            
            log.error("🤖 Gemini 응답 파싱 실패: {}", result);
            return "분석 결과를 가져올 수 없습니다.";
            
        } catch (Exception e) {
            log.error("🤖 Gemini API 호출 실패: {}", e.getMessage());
            return "AI 분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * 구독 분석 프롬프트 생성
     */
    public String createAnalysisPrompt(List<Map<String, Object>> subscriptions, Map<String, Object> stats) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 구독 서비스 지출 분석 전문가입니다.\n\n");
        prompt.append("사용자의 구독 정보를 분석해주세요.\n\n");
        
        // 구독 목록
        prompt.append("=== 구독 목록 ===\n");
        for (int i = 0; i < subscriptions.size(); i++) {
            Map<String, Object> sub = subscriptions.get(i);
            prompt.append(String.format("%d. %s - %s %,d원 (%s)\n",
                    i + 1,
                    sub.get("name"),
                    sub.get("cycle"),
                    sub.get("amount"),
                    sub.get("category")));
        }
        
        // 통계
        prompt.append("\n=== 통계 ===\n");
        prompt.append(String.format("- 총 구독 수: %d개\n", stats.get("totalCount")));
        prompt.append(String.format("- 월 평균 지출: %,d원\n", stats.get("monthlyAverage")));
        prompt.append(String.format("- 카테고리: %s\n", stats.get("categories")));
        
        // 분석 요청
        prompt.append("\n=== 분석 요청 ===\n");
        prompt.append("다음 형식으로 JSON으로 응답해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"한 줄 요약 (예: 총 3개 구독, 월 34,850원 지출)\",\n");
        prompt.append("  \"duplicates\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"category\": \"카테고리명\",\n");
        prompt.append("      \"subscriptions\": [\"구독1\", \"구독2\"],\n");
        prompt.append("      \"suggestion\": \"추천 내용\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"추천 제목\",\n");
        prompt.append("      \"description\": \"추천 설명\",\n");
        prompt.append("      \"estimatedSavings\": 예상절감액숫자\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"spendingTrend\": \"지출 트렌드 설명\",\n");
        prompt.append("  \"severity\": \"상황의 심각도 (high, normal, low 중 하나)\",\n");
        prompt.append("  \"confidence\": 0.95\n");
        prompt.append("}\n\n");
        prompt.append("JSON만 출력하고 다른 설명은 하지 마세요.");
        return prompt.toString();
    }

    /**
     * 월간 리포트 분석 프롬프트 생성
     */
    public String createReportPrompt(String month, List<Map<String, Object>> currentData, List<Map<String, Object>> prevData, Map<String, Integer> categoryBreakdown) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 개인 재무 분석 전문가입니다.\n\n");
        prompt.append(String.format("사용자의 %s 월간 지출 리포트를 분석하고 상세 의견을 주세요.\n\n", month));

        prompt.append("=== 이번 달 지출 내역 ===\n");
        for (Map<String, Object> sub : currentData) {
            prompt.append(String.format("- %s: %,d원\n", sub.get("name"), sub.get("amount")));
        }

        prompt.append("\n=== 카테고리별 지출 ===\n");
        categoryBreakdown.forEach((cat, amt) -> prompt.append(String.format("- %s: %,d원\n", cat, amt)));

        int currentTotal = currentData.stream().mapToInt(m -> (int)m.get("amount")).sum();
        int prevTotal = prevData.stream().mapToInt(m -> (int)m.get("amount")).sum();
        prompt.append(String.format("\n총 지출: %,d원 (지난달: %,d원)\n", currentTotal, prevTotal));

        prompt.append("\n=== 요청 사항 ===\n");
        prompt.append("다음 형식의 JSON으로만 응답해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"analysisSummary\": \"종합 분석 의견 (2~3문장)\",\n");
        prompt.append("  \"keyInsights\": [\"통찰1\", \"통찰2\", \"통찰3\"],\n");
        prompt.append("  \"topRecommendations\": [\n");
        prompt.append("    { \"title\": \"제목\", \"description\": \"설명\", \"estimatedSavings\": 0 }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");

        return prompt.toString();
    }
}
