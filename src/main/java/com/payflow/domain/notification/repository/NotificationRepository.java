package com.payflow.domain.notification.repository;

import com.payflow.domain.notification.entity.Notification;
import com.payflow.domain.notification.entity.Notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 특정 사용자의 모든 알림 조회
     */
    List<Notification> findByUserId(Long userId);

    /**
     * 특정 결제 주기에 대한 특정 타입 알림 존재 여부 확인 (중복 방지)
     */
    Optional<Notification> findByPaymentCycleIdAndType(Long paymentCycleId, NotificationType type);

    /**
     * 발송되지 않은 알림 조회
     */
    List<Notification> findBySentFalse();

    /**
     * 특정 사용자의 발송된 알림 조회
     */
    List<Notification> findByUserIdAndSentTrue(Long userId);

    /**
     * 결제 주기 삭제 전 알림 정리
     */
    void deleteByPaymentCycleIdIn(List<Long> paymentCycleIds);
}
