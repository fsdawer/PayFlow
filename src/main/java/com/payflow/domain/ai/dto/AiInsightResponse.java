package com.payflow.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIInsightResponse {
    private String title;
    private String summary;
    private List<DuplicateSubscription> duplicates;
    private List<SavingRecommendation> recommendations;
    private String spendingTrend;
    private Integer totalMonthlySpending;
    private Integer totalSubscriptions;
    private String severity;
    private Double confidence;
}
