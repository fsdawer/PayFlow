package com.payflow.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.domain.ai.dto.AIInsightResponse;
import com.payflow.domain.ai.dto.AIReportResponse;
import com.payflow.domain.ai.dto.SavingRecommendation;
import com.payflow.domain.ai.entity.AIReport;
import com.payflow.domain.ai.repository.AIReportRepository;
import com.payflow.domain.payment.entity.PaymentCycle;
import com.payflow.domain.payment.service.PaymentCycleService;
import com.payflow.domain.subscription.entity.Subscription;
import com.payflow.domain.subscription.repository.SubscriptionRepository;
import com.payflow.global.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReportServiceImpl implements AIReportService {

    private final PaymentCycleService paymentCycleService;
    private final SubscriptionRepository subscriptionRepository;
    private final AIAnalysisService aiAnalysisService;
    private final AIReportRepository aiReportRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Override
    public AIReportResponse generateMonthlyReport(Long userId, int year, int month) {
        // 0. DB에서 기존 리포트 확인
        Optional<AIReport> existingReport = aiReportRepository.findByUserIdAndYearAndMonth(userId, year, month);
        if (existingReport.isPresent()) {
            return readJson(existingReport.get().getReportJson(), AIReportResponse.class);
        }

        log.info("AI 월간 리포트 생성 중: userId={}, year={}, month={}", userId, year, month);

        // 1. 해당 월 데이터 조회
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        List<PaymentCycle> currentMonthCycles = paymentCycleService.getPaymentHistory(userId, startDate, endDate);

        // 2. 지난 달 데이터 조회 (비교용)
        LocalDate prevMonthStart = startDate.minusMonths(1);
        LocalDate prevMonthEnd = startDate.minusDays(1);
        List<PaymentCycle> prevMonthCycles = paymentCycleService.getPaymentHistory(userId, prevMonthStart, prevMonthEnd);

        // 3. 금액 계산
        int totalSpending = calculateTotal(currentMonthCycles);
        int prevSpending = calculateTotal(prevMonthCycles);
        int difference = totalSpending - prevSpending;

        // 4. 카테고리별 분석
        Map<String, Integer> categoryBreakdown = currentMonthCycles.stream()
                .collect(Collectors.groupingBy(
                        cycle -> {
                            Subscription sub = subscriptionRepository.findById(cycle.getSubscriptionId()).orElse(null);
                            return (sub != null && sub.getSubscriptionsCategory() != null) ? sub.getSubscriptionsCategory() : "기타";
                        },
                        Collectors.summingInt(cycle -> {
                            Subscription sub = subscriptionRepository.findById(cycle.getSubscriptionId()).orElse(null);
                            return (sub != null && sub.getAmount() != null) ? sub.getAmount() : 0;
                        })
                ));

        // 5. AI 리포트 생성 (Gemini 활용)
        String monthStr = String.format("%d년 %d월", year, month);
        String analysisSummary;
        List<String> keyInsights = new ArrayList<>();
        List<SavingRecommendation> recommendations;

        if (geminiClient.isConfigured()) {
            try {
                List<Map<String, Object>> currentData = currentMonthCycles.stream()
                        .map(this::toDataMap).collect(Collectors.toList());
                List<Map<String, Object>> prevData = prevMonthCycles.stream()
                        .map(this::toDataMap).collect(Collectors.toList());

                String prompt = geminiClient.createReportPrompt(monthStr, currentData, prevData, categoryBreakdown);
                String aiRes = geminiClient.callGemini(prompt);
                Map<String, Object> resultMap = objectMapper.readValue(extractJSON(aiRes), Map.class);

                analysisSummary = (String) resultMap.get("analysisSummary");
                keyInsights = (List<String>) resultMap.get("keyInsights");
                recommendations = parseRecommendations((List<Map>) resultMap.get("topRecommendations"));
            } catch (Exception e) {
                log.warn("AI 리포트 생성 실패, 로컬 분석으로 전환: {}", e.getMessage());
                analysisSummary = generateSummary(totalSpending, prevSpending, difference);
                AIInsightResponse insight = aiAnalysisService.analyzeSpendingPattern(userId);
                keyInsights.add(insight.getSpendingTrend());
                recommendations = insight.getRecommendations().stream().limit(3).collect(Collectors.toList());
            }
        } else {
            analysisSummary = generateSummary(totalSpending, prevSpending, difference);
            AIInsightResponse insight = aiAnalysisService.analyzeSpendingPattern(userId);
            keyInsights.add(insight.getSpendingTrend());
            recommendations = insight.getRecommendations().stream().limit(3).collect(Collectors.toList());
        }

        AIReportResponse response = AIReportResponse.builder()
                .month(monthStr)
                .totalSpending(totalSpending)
                .previousMonthSpending(prevSpending)
                .spendingDifference(difference)
                .analysisSummary(analysisSummary)
                .categoryBreakdown(categoryBreakdown)
                .keyInsights(keyInsights)
                .topRecommendations(recommendations)
                .build();

        // 7. DB 저장
        aiReportRepository.save(AIReport.builder()
                .userId(userId)
                .year(year)
                .month(month)
                .analysisSummary(analysisSummary)
                .reportJson(writeJson(response))
                .build());

        return response;
    }

    private int calculateTotal(List<PaymentCycle> cycles) {
        return cycles.stream()
                .mapToInt(cycle -> {
                    Subscription sub = subscriptionRepository.findById(cycle.getSubscriptionId()).orElse(null);
                    return (sub != null && sub.getAmount() != null) ? sub.getAmount() : 0;
                })
                .sum();
    }

    private Map<String, Object> toDataMap(PaymentCycle cycle) {
        Subscription sub = subscriptionRepository.findById(cycle.getSubscriptionId()).orElse(null);
        return Map.of(
            "name", sub != null ? sub.getSubscriptionsName() : "Unknown",
            "amount", (sub != null && sub.getAmount() != null) ? sub.getAmount() : 0
        );
    }

    private String extractJSON(String response) {
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        if (start != -1 && end != -1) return response.substring(start, end + 1);
        return "{}";
    }

    private List<SavingRecommendation> parseRecommendations(List<Map> list) {
        if (list == null) return new ArrayList<>();
        return list.stream()
                .map(m -> SavingRecommendation.builder()
                        .title((String) m.get("title"))
                        .description((String) m.get("description"))
                        .estimatedSavings(((Number) m.get("estimatedSavings")).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    private String generateSummary(int current, int prev, int diff) {
        if (prev == 0) {
            return String.format("이번 달 총 지출은 %,d원입니다. 첫 분석 달이네요!", current);
        }
        
        String changeStr = diff > 0 ? "증가" : "감소";
        return String.format("지난달 대비 지출이 %,d원 %s했습니다. (지난달: %,d원 → 이번달: %,d원)", 
                Math.abs(diff), changeStr, prev, current);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.error("JSON 직렬화 실패", e);
            return "{}";
        }
    }

    private <T> T readJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("JSON 역직렬화 실패", e);
            return null;
        }
    }
}
