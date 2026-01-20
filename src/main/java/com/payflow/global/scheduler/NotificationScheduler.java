package com.payflow.global.scheduler;

import com.payflow.domain.notification.service.NotificationService;
import com.payflow.domain.payment.entity.PaymentCycle;
import com.payflow.domain.payment.repository.PaymentCycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final PaymentCycleRepository paymentCycleRepository;
    private final NotificationService notificationService;
    /**
     * 매일 오전 9시 실행: 결제 리마인더 발송
     *
     * 테스트용: @Scheduled(fixedRate = 60000)  // 1분마다
     * 프로덕션: @Scheduled(cron = "0 0 9 * * *")  // 매일 오전 9시
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendDailyReminders() {
        log.info("=== 결제 리마인더 스케줄러 시작 ===");

        LocalDate today = LocalDate.now();
        LocalDate d3Date = today.plusDays(3);
        LocalDate d1Date = today.plusDays(1);
        // D-3 알림 발송
        List<PaymentCycle> d3Cycles = paymentCycleRepository
                .findByStatusAndDueDateBefore(PaymentCycle.PaymentStatus.PENDING, d3Date.plusDays(1))
                .stream()
                .filter(cycle -> cycle.getDueDate().equals(d3Date))
                .toList();

        log.info("D-3 알림 대상: {} 건", d3Cycles.size());
        for (PaymentCycle cycle : d3Cycles) {
            try {
                notificationService.sendPaymentReminder(cycle, 3);
            } catch (Exception e) {
                log.error("D-3 알림 발송 실패: cycleId={}, error={}", cycle.getCycleId(), e.getMessage());
            }
        }
        // D-1 알림 발송
        List<PaymentCycle> d1Cycles = paymentCycleRepository
                .findByStatusAndDueDateBefore(PaymentCycle.PaymentStatus.PENDING, d1Date.plusDays(1))
                .stream()
                .filter(cycle -> cycle.getDueDate().equals(d1Date))
                .toList();

        log.info("D-1 알림 대상: {} 건", d1Cycles.size());
        for (PaymentCycle cycle : d1Cycles) {
            try {
                notificationService.sendPaymentReminder(cycle, 1);
            } catch (Exception e) {
                log.error("D-1 알림 발송 실패: cycleId={}, error={}", cycle.getCycleId(), e.getMessage());
            }
        }
        log.info("=== 결제 리마인더 스케줄러 종료 ===");
    }


    /**
     * 매일 새벽 2시 실행: 연체 처리 및 알림 발송
     *
     * 테스트용: @Scheduled(fixedRate = 120000)  // 2분마다
     * 프로덕션: @Scheduled(cron = "0 0 2 * * *")  // 매일 새벽 2시
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void processOverduePayments() {
        log.info("=== 연체 처리 스케줄러 시작 ===");

        LocalDate today = LocalDate.now();
        // 결제일이 지났는데 PENDING 상태인 것들 찾기
        List<PaymentCycle> overdueCycles = paymentCycleRepository
                .findByStatusAndDueDateBefore(PaymentCycle.PaymentStatus.PENDING, today);
        log.info("연체 처리 대상: {} 건", overdueCycles.size());

        for (PaymentCycle cycle : overdueCycles) {
            try {
                // 연체 상태로 변경
                cycle.markAsOverdue();
                paymentCycleRepository.save(cycle);

                // 연체 알림 발송
                notificationService.sendOverdueNotification(cycle);

                log.info("연체 처리 완료: cycleId={}, subscriptionId={}",
                        cycle.getCycleId(), cycle.getSubscriptionId());
            } catch (Exception e) {
                log.error("연체 처리 실패: cycleId={}, error={}", cycle.getCycleId(), e.getMessage());
            }
        }
        log.info("=== 연체 처리 스케줄러 종료 ===");
    }
}
