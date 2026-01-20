package com.payflow.domain.notification.service;

import com.payflow.domain.payment.entity.PaymentCycle;

public interface NotificationService {

    /**
     * 결제 리마인더 알림 발송 (D-3, D-1)
     * @param cycle 결제 주기
     * @param daysAhead 며칠 전 알림인지 (3 또는 1)
     */
    void sendPaymentReminder(PaymentCycle cycle, int daysAhead);

    /**
     * 연체 알림 발송
     * @param cycle 결제 주기
     */
    void sendOverdueNotification(PaymentCycle cycle);
}
