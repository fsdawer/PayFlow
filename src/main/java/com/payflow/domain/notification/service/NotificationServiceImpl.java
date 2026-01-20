package com.payflow.domain.notification.service;

import com.payflow.domain.notification.entity.Notification;
import com.payflow.domain.notification.repository.NotificationRepository;
import com.payflow.domain.payment.entity.PaymentCycle;
import com.payflow.domain.subscription.entity.Subscription;
import com.payflow.domain.subscription.repository.SubscriptionRepository;
import com.payflow.domain.user.entity.User;
import com.payflow.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;


    /**
     * 결제 리마인더 알림 생성 및 발송
     */
    @Transactional
    @Override
    public void sendPaymentReminder(PaymentCycle cycle, int daysAhead) {
        // 구독 정보 조회
        Subscription subscription = subscriptionRepository.findById(cycle.getSubscriptionId())
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        // 알림 설정 확인
        boolean shouldSend = (daysAhead == 3 && subscription.getReminderD3()) ||
                (daysAhead == 1 && subscription.getReminderD1());

        if(!shouldSend) {
            return;
        }

        // 알림 타입 설정
        Notification.NotificationType type = daysAhead == 3 ? Notification.NotificationType.D3_REMINDER : Notification.NotificationType.D1_REMINDER;

        // 중복 발송 방지
        if (notificationRepository.findByPaymentCycleIdAndType(cycle.getCycleId(), type).isPresent()) {
            log.info("이미 발송된 알림: paymentCycleId={}, type={}", cycle.getCycleId(), type);
            return;
        }

        // 알림 메시지 생성
        String message = String.format(
                "%s 구독 결제일(%s)이 %d일 남았습니다. (₩%,d원)",
                subscription.getSubscriptionsName(),
                cycle.getDueDate(),
                daysAhead,
                subscription.getAmount()
        );

        // 알림 생성 및 저장
        Notification notification = Notification.builder()
                .userId(subscription.getUserId())
                .paymentCycleId(cycle.getCycleId())
                .type(type)
                .message(message)
                .build();
        notificationRepository.save(notification);

        // 사용자 이메일 조회
        User user = userRepository.findById(subscription.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // HTML 이메일 발송
        try {
            String subject = String.format("[PayFlow] %s 결제 D-%d 알림", subscription.getSubscriptionsName(), daysAhead);
            String htmlBody = emailService.createPaymentReminderHtml(
                    subscription.getSubscriptionsName(),
                    cycle.getDueDate().toString(),
                    daysAhead,
                    subscription.getAmount()
            );
            emailService.sendHtmlEmail(user.getEmail(), subject, htmlBody);
            
            // 발송 완료 처리
            notification.markAsSent();
            notificationRepository.save(notification);
            
            log.info("📧 [이메일 발송 성공] to={}, message={}", user.getEmail(), message);
        } catch (Exception e) {
            log.error("📧 [이메일 발송 실패] userId={}, error={}", user.getUserId(), e.getMessage());
            // 이메일 발송 실패해도 알림 기록은 저장 (재시도 가능하도록)
        }
    }


    /**
     * 연체 알림 생성 및 발송
     */
    @Transactional
    public void sendOverdueNotification(PaymentCycle cycle) {
        // 구독 정보 조회
        Subscription subscription = subscriptionRepository.findById(cycle.getSubscriptionId())
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다"));

        // 중복 발송 방지
        if (notificationRepository.findByPaymentCycleIdAndType(cycle.getCycleId(), Notification.NotificationType.OVERDUE).isPresent()) {
            return;
        }
        // 알림 메시지 생성
        String message = String.format(
                "⚠️ %s 구독 결제일(%s)이 지났습니다. 연체료가 발생할 수 있습니다. (₩%,d원)",
                subscription.getSubscriptionsName(),
                cycle.getDueDate(),
                subscription.getAmount()
        );
        // 알림 생성 및 저장
        Notification notification = Notification.builder()
                .userId(subscription.getUserId())
                .paymentCycleId(cycle.getCycleId())
                .type(Notification.NotificationType.OVERDUE)
                .message(message)
                .build();
        notificationRepository.save(notification);

        // 사용자 이메일 조회
        User user = userRepository.findById(subscription.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // HTML 이메일 발송
        try {
            String subject = String.format("⚠️ [PayFlow] %s 결제 연체 알림", subscription.getSubscriptionsName());
            String htmlBody = emailService.createOverdueNotificationHtml(
                    subscription.getSubscriptionsName(),
                    cycle.getDueDate().toString(),
                    subscription.getAmount()
            );
            emailService.sendHtmlEmail(user.getEmail(), subject, htmlBody);
            
            // 발송 완료 처리
            notification.markAsSent();
            notificationRepository.save(notification);
            
            log.warn("📧 [연체 이메일 발송 성공] to={}, message={}", user.getEmail(), message);
        } catch (Exception e) {
            log.error("📧 [연체 이메일 발송 실패] userId={}, error={}", user.getUserId(), e.getMessage());
            // 이메일 발송 실패해도 알림 기록은 저장
        }
    }
}