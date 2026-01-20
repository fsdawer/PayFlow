package com.payflow.domain.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "payment_cycle_id")
    private Long paymentCycleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private boolean sent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(nullable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 발송 완료 처리
     */
    public void markAsSent() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
    }

    public enum NotificationType {
        D3_REMINDER,   // 3일 전 알림
        D1_REMINDER,   // 1일 전 알림
        OVERDUE        // 연체 알림
    }
}
