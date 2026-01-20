package com.payflow.domain.ai.service;

import com.payflow.domain.ai.dto.AIInsightResponse;
import java.util.Optional;

public interface AIInsightService {
    /**
     * 최신 AI 인사이트 조회
     */
    Optional<AIInsightResponse> getLatestInsight(Long userId);

    /**
     * AI 인사이트 재생성 및 저장
     */
    AIInsightResponse refreshInsight(Long userId);
}
