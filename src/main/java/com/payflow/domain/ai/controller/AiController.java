package com.payflow.domain.ai.controller;

import com.payflow.domain.ai.service.AiInsightService;
import com.payflow.domain.ai.service.AiReportService;

public class AiController {
  private final AiReportService aiReportService;
  private final AiInsightService aiInsightService;

  public AiController(AiReportService aiReportService, AiInsightService aiInsightService) {
    this.aiReportService = aiReportService;
    this.aiInsightService = aiInsightService;
  }
}
