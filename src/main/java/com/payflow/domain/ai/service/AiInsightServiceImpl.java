package com.payflow.domain.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.domain.ai.dto.AIInsightResponse;
import com.payflow.domain.ai.dto.DuplicateSubscription;
import com.payflow.domain.ai.dto.SavingRecommendation;
import com.payflow.domain.ai.entity.AIInsight;
import com.payflow.domain.ai.repository.AIInsightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIInsightServiceImpl implements AIInsightService {

    private final AIAnalysisService aiAnalysisService;
    private final AIInsightRepository aiInsightRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<AIInsightResponse> getLatestInsight(Long userId) {
        return aiInsightRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public AIInsightResponse refreshInsight(Long userId) {
        AIInsightResponse response = aiAnalysisService.analyzeSpendingPattern(userId);
        aiInsightRepository.save(toEntity(userId, response));
        return response;
    }

    private AIInsight toEntity(Long userId, AIInsightResponse response) {
        return AIInsight.builder()
                .userId(userId)
                .insightType("SPENDING")
                .title(response.getTitle() == null ? "스마트 구독 분석 리포트" : response.getTitle())
                .summary(response.getSummary() == null ? "" : response.getSummary())
                .reasonText((response.getSummary() == null ? "" : response.getSummary())
                        + "\n"
                        + (response.getSpendingTrend() == null ? "" : response.getSpendingTrend()))
                .suggestionText(writeJson(response.getRecommendations()) + "\n" + writeJson(response.getDuplicates()))
                .recommendationsJson(writeJson(response.getRecommendations()))
                .duplicatesJson(writeJson(response.getDuplicates()))
                .spendingTrend(response.getSpendingTrend())
                .totalMonthlySpending(response.getTotalMonthlySpending() == null ? 0 : response.getTotalMonthlySpending())
                .totalSubscriptions(response.getTotalSubscriptions() == null ? 0 : response.getTotalSubscriptions())
                .severity(response.getSeverity() == null ? "normal" : response.getSeverity())
                .confidence(response.getConfidence() == null ? 0.95 : response.getConfidence())
                .inputSnapshot(writeJson(Map.of(
                    "totalSubscriptions", response.getTotalSubscriptions() == null ? 0 : response.getTotalSubscriptions(),
                    "totalMonthlySpending", response.getTotalMonthlySpending() == null ? 0 : response.getTotalMonthlySpending()
                )))
                .build();
    }

    private AIInsightResponse toResponse(AIInsight entity) {
        String suggestionText = entity.getSuggestionText() == null ? "" : entity.getSuggestionText();
        String[] suggestions = suggestionText.split("\n");
        
        return AIInsightResponse.builder()
                .title(entity.getTitle())
                .summary(entity.getSummary() != null ? entity.getSummary() : entity.getReasonText())
                .recommendations(readJson(
                        entity.getRecommendationsJson() != null ? entity.getRecommendationsJson() : (suggestions.length > 0 ? suggestions[0] : "[]"),
                        new TypeReference<List<SavingRecommendation>>() {}))
                .duplicates(readJson(
                        entity.getDuplicatesJson() != null ? entity.getDuplicatesJson() : (suggestions.length > 1 ? suggestions[1] : "[]"),
                        new TypeReference<List<DuplicateSubscription>>() {}))
                .spendingTrend(entity.getSpendingTrend())
                .totalMonthlySpending(entity.getTotalMonthlySpending())
                .totalSubscriptions(entity.getTotalSubscriptions())
                .severity(entity.getSeverity())
                .confidence(entity.getConfidence())
                .build();
    }

    private String writeJson(Object value) {
        try {
            return value == null ? "[]" : objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("AI 인사이트 JSON 직렬화 실패: {}", e.getMessage());
            return "[]";
        }
    }

    private <T> List<T> readJson(String json, TypeReference<List<T>> type) {
        try {
            if (json == null || json.isBlank()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("AI 인사이트 JSON 역직렬화 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
