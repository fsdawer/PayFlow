package com.payflow.domain.payment.service;

import com.payflow.domain.payment.entity.PaymentCycle;

import java.time.LocalDate;
import java.util.List;

public interface PaymentCycleService {

    /**
     * 구독에 대한 결제 주기를 자동 생성 (향후 N개월치)
     * @param subscriptionId 구독 ID
     * @param monthsAhead 미래 몇 개월치 생성할지 (기본: 12개월)
     */
    void createPaymentCyclesForSubscription(Long subscriptionId, int monthsAhead);

    /**
     * 사용자의 다가오는 결제 조회
     * @param userId 사용자 ID
     * @param days 며칠 이내의 결제 조회
     * @return 다가오는 결제 리스트
     */
    List<PaymentCycle> getUpcomingPayments(Long userId, int days);

    /**
     * 결제 완료 처리
     * @param cycleId 결제 주기 ID
     * @param paidAmount 실제 결제 금액
     */
    void markAsPaid(Long cycleId, Integer paidAmount);

    /**
     * 연체 처리
     * @param cycleId 결제 주기 ID
     */
    void markAsOverdue(Long cycleId);

    /**
     * 사용자의 결제 이력 조회
     * @param userId 사용자 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 결제 이력 리스트
     */
    List<PaymentCycle> getPaymentHistory(Long userId, LocalDate startDate, LocalDate endDate);
}
