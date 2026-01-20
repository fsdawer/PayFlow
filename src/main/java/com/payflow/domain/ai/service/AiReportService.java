package com.payflow.domain.ai.service;

import com.payflow.domain.ai.dto.AIReportResponse;

public interface AIReportService {
    /**
     * 특정 월의 AI 리포트 생성
     */
    AIReportResponse generateMonthlyReport(Long userId, int year, int month);
}
