package com.payflow.domain.ai.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIReportResponse {
    private String month;
    private int totalSpending;
    private int previousMonthSpending;
    private int spendingDifference;
    private String analysisSummary;
    private Map<String, Integer> categoryBreakdown;
    private List<String> keyInsights;
    private List<SavingRecommendation> topRecommendations;
}
