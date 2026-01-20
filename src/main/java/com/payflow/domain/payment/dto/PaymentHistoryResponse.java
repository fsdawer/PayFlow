package com.payflow.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 결제 이력 응답 DTO
 * 월별 결제 내역 조회 시 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistoryResponse {

    /**
     * 결제 이력 목록
     */
    private List<PaymentCycleResponse> payments;

    /**
     * 조회 기간 시작일
     */
    private LocalDate startDate;

    /**
     * 조회 기간 종료일
     */
    private LocalDate endDate;

    /**
     * 총 결제 금액 (PAID 상태만)
     */
    private Integer totalPaidAmount;

    /**
     * 총 결제 건수 (PAID 상태만)
     */
    private int paidCount;

    /**
     * 연체 건수
     */
    private int overdueCount;

    /**
     * 미결제 건수
     */
    private int pendingCount;

    /**
     * 월간 총 지출 예정 금액 (모든 상태 합계)
     */
    private Integer totalMonthlySpending;

    /**
     * 카테고리별 지출 금액
     * 예: {"스트리밍": 50000, "클라우드": 30000}
     */
    private Map<String, Integer> categoryExpenses;

    /**
     * 편의 메서드: 통계 계산
     */
    public static PaymentHistoryResponse from(
            List<PaymentCycleResponse> payments,
            LocalDate startDate,
            LocalDate endDate
    ) {
        // PAID 상태 금액 합계
        int totalPaidAmount = payments.stream()
                .filter(p -> p.getStatus() == com.payflow.domain.payment.entity.PaymentCycle.PaymentStatus.PAID)
                .map(p -> p.getPaidAmount() != null ? p.getPaidAmount() : p.getSubscriptionAmount())
                .filter(amount -> amount != null)
                .mapToInt(Integer::intValue)
                .sum();

        // 모든 상태 금액 합계
        int totalMonthlySpending = payments.stream()
                .map(p -> p.getPaidAmount() != null ? p.getPaidAmount() : p.getSubscriptionAmount())
                .filter(amount -> amount != null)
                .mapToInt(Integer::intValue)
                .sum();

        // 각 상태별 건수
        long paidCount = payments.stream()
                .filter(p -> p.getStatus() == com.payflow.domain.payment.entity.PaymentCycle.PaymentStatus.PAID)
                .count();

        long overdueCount = payments.stream()
                .filter(p -> p.getStatus() == com.payflow.domain.payment.entity.PaymentCycle.PaymentStatus.OVERDUE)
                .count();

        long pendingCount = payments.stream()
                .filter(p -> p.getStatus() == com.payflow.domain.payment.entity.PaymentCycle.PaymentStatus.PENDING)
                .count();

        // 카테고리별 지출 (PAID만)
        Map<String, Integer> categoryExpenses = payments.stream()
                .filter(p -> p.getStatus() == com.payflow.domain.payment.entity.PaymentCycle.PaymentStatus.PAID)
                .filter(p -> p.getSubscriptionCategory() != null)
                .collect(
                        java.util.stream.Collectors.groupingBy(
                                PaymentCycleResponse::getSubscriptionCategory,
                                java.util.stream.Collectors.summingInt(p ->
                                        p.getPaidAmount() != null ? p.getPaidAmount() : (p.getSubscriptionAmount() == null ? 0 : p.getSubscriptionAmount())
                                )
                        )
                );

        return PaymentHistoryResponse.builder()
                .payments(payments)
                .startDate(startDate)
                .endDate(endDate)
                .totalPaidAmount(totalPaidAmount)
                .totalMonthlySpending(totalMonthlySpending)
                .paidCount((int) paidCount)
                .overdueCount((int) overdueCount)
                .pendingCount((int) pendingCount)
                .categoryExpenses(categoryExpenses)
                .build();
    }
}
