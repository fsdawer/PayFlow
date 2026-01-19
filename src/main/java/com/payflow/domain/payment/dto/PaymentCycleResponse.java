package com.payflow.domain.payment.dto;

import com.payflow.domain.payment.entity.PaymentCycle.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 결제 주기 응답 DTO
 * 단일 PaymentCycle 조회 시 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCycleResponse {

    private Long cycleId;
    private Long subscriptionId;
    private LocalDate dueDate;
    private PaymentStatus status;
    private Integer paidAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 추가 정보: 구독 정보 (JOIN 시 사용)
    private String subscriptionName;      // 구독 서비스명
    private Integer subscriptionAmount;   // 구독 금액
    private String subscriptionCategory;  // 구독 카테고리

    /**
     * Entity → DTO 변환 (기본)
     */
    public static PaymentCycleResponse from(com.payflow.domain.payment.entity.PaymentCycle cycle) {
        return PaymentCycleResponse.builder()
                .cycleId(cycle.getCycleId())
                .subscriptionId(cycle.getSubscriptionId())
                .dueDate(cycle.getDueDate())
                .status(cycle.getStatus())
                .paidAmount(cycle.getPaidAmount())
                .createdAt(cycle.getCreatedAt())
                .updatedAt(cycle.getUpdatedAt())
                .build();
    }

    /**
     * Entity → DTO 변환 (구독 정보 포함)
     * 목록 조회 시 사용자에게 더 많은 정보 제공
     */
    public static PaymentCycleResponse from(
            com.payflow.domain.payment.entity.PaymentCycle cycle,
            com.payflow.domain.subscription.entity.Subscription subscription
    ) {
        return PaymentCycleResponse.builder()
                .cycleId(cycle.getCycleId())
                .subscriptionId(cycle.getSubscriptionId())
                .dueDate(cycle.getDueDate())
                .status(cycle.getStatus())
                .paidAmount(cycle.getPaidAmount())
                .createdAt(cycle.getCreatedAt())
                .updatedAt(cycle.getUpdatedAt())
                // 구독 정보 추가
                .subscriptionName(subscription.getSubscriptionsName())
                .subscriptionAmount(subscription.getAmount())
                .subscriptionCategory(subscription.getSubscriptionsCategory())
                .build();
    }
}
