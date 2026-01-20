package com.payflow.domain.ai.controller;

import com.payflow.domain.ai.dto.AIInsightResponse;
import com.payflow.domain.ai.dto.AIReportResponse;
import com.payflow.domain.ai.service.AIInsightService;
import com.payflow.domain.ai.service.AIReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AIAnalysisController {

    private final AIInsightService aiInsightService;
    private final AIReportService aiReportService;

    /**
     * AI 인사이트 조회
     */
    @GetMapping("/insights")
    public ResponseEntity<AIInsightResponse> getInsights(
            @AuthenticationPrincipal Long userId) {
        
        log.info("AI 인사이트 요청: userId={}", userId);
        return aiInsightService.getLatestInsight(userId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.ok(aiInsightService.refreshInsight(userId)));
    }

    /**
     * AI 인사이트 재생성 및 저장
     */
    @PostMapping("/insights/refresh")
    public ResponseEntity<AIInsightResponse> refreshInsights(
            @AuthenticationPrincipal Long userId) {
        log.info("AI 인사이트 재생성 요청: userId={}", userId);
        return ResponseEntity.ok(aiInsightService.refreshInsight(userId));
    }

    /**
     * 월간 AI 리포트 생성
     */
    @GetMapping("/report")
    public ResponseEntity<AIReportResponse> getMonthlyReport(
            @AuthenticationPrincipal Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        log.info("AI 월간 리포트 요청: userId={}, year={}, month={}", userId, year, month);
        return ResponseEntity.ok(aiReportService.generateMonthlyReport(userId, year, month));
    }
}
