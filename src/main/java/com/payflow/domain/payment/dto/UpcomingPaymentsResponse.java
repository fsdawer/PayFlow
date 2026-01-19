package com.payflow.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 다가오는 결제 목록 응답 DTO
 * 대시보드에서 "곧 결제될 항목" 표시용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingPaymentsResponse {

    /**
     * 다가오는 결제 목록
     */
    private List<PaymentCycleResponse> payments;

    /**
     * 조회 기준일
     */
    private LocalDate baseDate;

    /**
     * 조회 범위 (일 수)
     */
    private int daysAhead;

    /**
     * 총 결제 예정 금액
     */
    private Integer totalAmount;

    /**
     * 결제 예정 건수
     */
    private int count;

    /**
     * 편의 메서드: 총 금액 계산
     */
    public static UpcomingPaymentsResponse from(
            List<PaymentCycleResponse> payments,
            int daysAhead
    ) {
        int totalAmount = payments.stream()
                .map(PaymentCycleResponse::getSubscriptionAmount)
                .filter(amount -> amount != null)
                .mapToInt(Integer::intValue)
                .sum();

        return UpcomingPaymentsResponse.builder()
                .payments(payments)
                .baseDate(LocalDate.now())
                .daysAhead(daysAhead)
                .totalAmount(totalAmount)
                .count(payments.size())
                .build();
    }
}
