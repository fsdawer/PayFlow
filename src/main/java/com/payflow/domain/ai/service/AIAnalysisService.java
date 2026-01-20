package com.payflow.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.domain.ai.dto.AIInsightResponse;
import com.payflow.domain.ai.dto.DuplicateSubscription;
import com.payflow.domain.ai.dto.SavingRecommendation;
import com.payflow.domain.subscription.entity.Subscription;
import com.payflow.domain.subscription.repository.SubscriptionRepository;
import com.payflow.global.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AIAnalysisService {

    private final SubscriptionRepository subscriptionRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public AIAnalysisService(SubscriptionRepository subscriptionRepository, 
                            GeminiClient geminiClient, 
                            ObjectMapper objectMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * AI 인사이트 분석
     */
    public AIInsightResponse analyzeSpendingPattern(Long userId) {
        try {
            // 1. 사용자의 활성 구독 조회
            List<Subscription> subscriptions = subscriptionRepository.findByUserIdAndStatus(
                    userId, Subscription.Status.ACTIVE);

            if (subscriptions.isEmpty()) {
                return createEmptyResponse();
            }

            if (!geminiClient.isConfigured()) {
                log.info("Gemini API 키 미설정: 로컬 분석으로 대체합니다.");
                return analyzeLocally(subscriptions);
            }

            // 2. 구독 데이터 변환
            List<Map<String, Object>> subData = subscriptions.stream()
                    .map(sub -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("name", sub.getSubscriptionsName());
                        data.put("amount", sub.getAmount());
                        data.put("cycle", getCycleLabel(sub.getCycleType()));
                        data.put("category", sub.getSubscriptionsCategory() != null ? 
                                sub.getSubscriptionsCategory() : "기타");
                        data.put("monthlyEquivalent", toMonthlyAmount(sub));
                        return data;
                    })
                    .collect(Collectors.toList());

            // 3. 통계 계산
            Map<String, Object> stats = calculateStats(subscriptions);

            // 4. Gemini API 호출
            String prompt = geminiClient.createAnalysisPrompt(subData, stats);
            String geminiResponse = geminiClient.callGemini(prompt);

            // 5. JSON 파싱 (Gemini 응답에서 JSON 부분만 추출)
            String jsonResponse = extractJSON(geminiResponse);
            
            // 6. DTO 변환
            Map<String, Object> resultMap = objectMapper.readValue(jsonResponse, Map.class);
            
            return AIInsightResponse.builder()
                    .title("스마트 구독 분석 리포트")
                    .summary((String) resultMap.get("summary"))
                    .duplicates(parseDuplicates((List<Map>) resultMap.get("duplicates")))
                    .recommendations(parseRecommendations((List<Map>) resultMap.get("recommendations")))
                    .spendingTrend((String) resultMap.getOrDefault("spendingTrend", "데이터가 부족합니다"))
                    .totalMonthlySpending((Integer) stats.get("monthlyAverage"))
                    .totalSubscriptions(subscriptions.size())
                    .severity((String) resultMap.getOrDefault("severity", "normal"))
                    .confidence((Double) resultMap.getOrDefault("confidence", 0.95))
                    .build();

        } catch (Exception e) {
            log.error("AI 분석 실패: userId={}, error={}", userId, e.getMessage(), e);
            return analyzeLocallyWithFallback(e.getMessage(), userId);
        }
    }

    private String getCycleLabel(Subscription.CycleType cycleType) {
        return switch (cycleType) {
            case MONTHLY -> "월";
            case YEARLY -> "연";
            case WEEKLY -> "주";
        };
    }

    private Map<String, Object> calculateStats(List<Subscription> subscriptions) {
        Map<String, Object> stats = new HashMap<>();
        
        // 월 평균 지출 계산 (모든 주기 월 환산)
        int monthlyTotal = subscriptions.stream()
                .mapToInt(this::toMonthlyAmount)
                .sum();
        
        stats.put("totalCount", subscriptions.size());
        stats.put("monthlyAverage", monthlyTotal);
        
        // 카테고리 집계
        Set<String> categories = subscriptions.stream()
                .map(sub -> sub.getSubscriptionsCategory() != null ? 
                        sub.getSubscriptionsCategory() : "기타")
                .collect(Collectors.toSet());
        stats.put("categories", String.join(", ", categories));
        
        return stats;
    }

    private int toMonthlyAmount(Subscription subscription) {
        int amount = subscription.getAmount() == null ? 0 : subscription.getAmount();
        return switch (subscription.getCycleType()) {
            case MONTHLY -> amount;
            case WEEKLY -> (int) Math.round(amount * 4.0);
            case YEARLY -> (int) Math.round(amount / 12.0);
        };
    }

    private AIInsightResponse analyzeLocallyWithFallback(String error, Long userId) {
        try {
            List<Subscription> subscriptions = subscriptionRepository.findByUserIdAndStatus(
                    userId, Subscription.Status.ACTIVE);
            if (subscriptions.isEmpty()) {
                return createEmptyResponse();
            }
            AIInsightResponse response = analyzeLocally(subscriptions);
            return AIInsightResponse.builder()
                    .summary(response.getSummary())
                    .duplicates(response.getDuplicates())
                    .recommendations(response.getRecommendations())
                    .spendingTrend(response.getSpendingTrend())
                    .totalMonthlySpending(response.getTotalMonthlySpending())
                    .totalSubscriptions(response.getTotalSubscriptions())
                    .build();
        } catch (Exception fallbackError) {
            return createErrorResponse(error);
        }
    }

    private AIInsightResponse analyzeLocally(List<Subscription> subscriptions) {
        int totalMonthly = subscriptions.stream()
                .mapToInt(this::toMonthlyAmount)
                .sum();

        String summary = String.format("총 %d개 구독, 월 평균 %,d원 지출", subscriptions.size(), totalMonthly);

        Map<String, List<Subscription>> byCategory = subscriptions.stream()
                .collect(Collectors.groupingBy(sub ->
                        sub.getSubscriptionsCategory() != null ? sub.getSubscriptionsCategory() : "기타"));

        List<DuplicateSubscription> duplicates = new ArrayList<>();
        List<SavingRecommendation> recommendations = new ArrayList<>();

        for (Map.Entry<String, List<Subscription>> entry : byCategory.entrySet()) {
            List<Subscription> list = entry.getValue();
            if (list.size() < 2) continue;

            String[] names = list.stream()
                    .map(Subscription::getSubscriptionsName)
                    .filter(Objects::nonNull)
                    .toArray(String[]::new);

            int categoryMonthly = list.stream().mapToInt(this::toMonthlyAmount).sum();
            int minMonthly = list.stream().mapToInt(this::toMonthlyAmount).min().orElse(0);
            int potential = Math.max(0, categoryMonthly - minMonthly);

            duplicates.add(DuplicateSubscription.builder()
                    .category(entry.getKey())
                    .subscriptions(names)
                    .suggestion(String.format("%s 카테고리에 %d개 구독이 있어요. 하나로 정리하면 절감할 수 있습니다.",
                            entry.getKey(), list.size()))
                    .build());

            if (potential > 0) {
                recommendations.add(SavingRecommendation.builder()
                        .title(String.format("%s 중복 구독 정리", entry.getKey()))
                        .description(String.format("해당 카테고리에서 가장 합리적인 구독 1개만 유지해 보세요."))
                        .estimatedSavings(potential)
                        .build());
            }
        }

        Subscription top = subscriptions.stream()
                .max(Comparator.comparingInt(this::toMonthlyAmount))
                .orElse(null);

        if (top != null) {
            int topMonthly = toMonthlyAmount(top);
            int estimatedSavings = Math.max(0, (int) Math.round(topMonthly * 0.15));
            recommendations.add(SavingRecommendation.builder()
                    .title("고액 구독 점검")
                    .description(String.format("%s 구독은 월 환산 %,d원입니다. 다운그레이드 옵션을 확인해 보세요.",
                            top.getSubscriptionsName(), topMonthly))
                    .estimatedSavings(estimatedSavings)
                    .build());
        }

        Map<String, Integer> categoryTotals = new HashMap<>();
        for (Map.Entry<String, List<Subscription>> entry : byCategory.entrySet()) {
            int sum = entry.getValue().stream().mapToInt(this::toMonthlyAmount).sum();
            categoryTotals.put(entry.getKey(), sum);
        }

        String spendingTrend = "데이터가 부족합니다";
        if (!categoryTotals.isEmpty()) {
            Map.Entry<String, Integer> topCategory = categoryTotals.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            if (topCategory != null) {
                spendingTrend = String.format("가장 큰 지출은 %s (월 환산 %,d원)입니다.",
                        topCategory.getKey(), topCategory.getValue());
            }
        }

        return AIInsightResponse.builder()
                .summary(summary)
                .duplicates(duplicates)
                .recommendations(recommendations)
                .spendingTrend(spendingTrend)
                .totalMonthlySpending(totalMonthly)
                .totalSubscriptions(subscriptions.size())
                .severity("normal")
                .confidence(1.0)
                .build();
    }

    private String extractJSON(String response) {
        // Gemini 응답에서 JSON 부분만 추출
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        
        if (start != -1 && end != -1) {
            return response.substring(start, end + 1);
        }
        
        // JSON이 없으면 기본 응답
        return "{\"summary\":\"분석 결과를 생성할 수 없습니다\",\"duplicates\":[],\"recommendations\":[]}";
    }

    private List<DuplicateSubscription> parseDuplicates(List<Map> duplicates) {
        if (duplicates == null) return new ArrayList<>();
        
        List<DuplicateSubscription> list = new ArrayList<>();
        for (Map dup : duplicates) {
            try {
                Object subObj = dup.get("subscriptions");
                String[] subs;
                if (subObj instanceof List) {
                    subs = ((List<String>) subObj).toArray(new String[0]);
                } else {
                    subs = new String[0];
                }

                list.add(DuplicateSubscription.builder()
                        .category(String.valueOf(dup.getOrDefault("category", "기타")))
                        .subscriptions(subs)
                        .suggestion(String.valueOf(dup.getOrDefault("suggestion", "")))
                        .build());
            } catch (Exception e) {
                log.warn("중복 구독 데이터 파싱 실패: {}", e.getMessage());
            }
        }
        return list;
    }

    private List<SavingRecommendation> parseRecommendations(List<Map> recommendations) {
        if (recommendations == null) return new ArrayList<>();
        
        List<SavingRecommendation> list = new ArrayList<>();
        for (Map rec : recommendations) {
            try {
                Object savingsObj = rec.get("estimatedSavings");
                int savings = 0;
                if (savingsObj instanceof Number) {
                    savings = ((Number) savingsObj).intValue();
                }

                list.add(SavingRecommendation.builder()
                        .title(String.valueOf(rec.getOrDefault("title", "절감 제안")))
                        .description(String.valueOf(rec.getOrDefault("description", "")))
                        .estimatedSavings(savings)
                        .build());
            } catch (Exception e) {
                log.warn("절감 추천 데이터 파싱 실패: {}", e.getMessage());
            }
        }
        return list;
    }

    private AIInsightResponse createEmptyResponse() {
        return AIInsightResponse.builder()
                .summary("등록된 구독이 없습니다")
                .duplicates(new ArrayList<>())
                .recommendations(new ArrayList<>())
                .spendingTrend("구독을 등록하면 지출 패턴을 분석해드립니다")
                .totalMonthlySpending(0)
                .totalSubscriptions(0)
                .build();
    }

    private AIInsightResponse createErrorResponse(String error) {
        return AIInsightResponse.builder()
                .summary("분석 중 오류가 발생했습니다")
                .duplicates(new ArrayList<>())
                .recommendations(List.of(
                        SavingRecommendation.builder()
                                .title("오류 발생")
                                .description(error)
                                .estimatedSavings(0)
                                .build()
                ))
                .spendingTrend("잠시 후 다시 시도해주세요")
                .totalMonthlySpending(0)
                .totalSubscriptions(0)
                .build();
    }
}
