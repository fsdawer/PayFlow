package com.payflow.domain.payment.repository;

import com.payflow.domain.payment.entity.PaymentCycle;
import com.payflow.domain.payment.entity.PaymentCycle.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentCycleRepository extends JpaRepository<PaymentCycle, Long> {

    /**
     * 특정 구독의 모든 결제 주기 조회
     */
    List<PaymentCycle> findBySubscriptionId(Long subscriptionId);

    /**
     * 특정 구독의 특정 상태 결제 주기 조회
     */
    List<PaymentCycle> findBySubscriptionIdAndStatus(Long subscriptionId, PaymentStatus status);

    /**
     * 특정 날짜 이전의 특정 상태 결제 주기 조회 (연체 처리용)
     */
    List<PaymentCycle> findByStatusAndDueDateBefore(PaymentStatus status, LocalDate date);

    /**
     * 사용자의 다가오는 결제 조회 (JOIN 필요)
     */
    @Query("SELECT pc FROM PaymentCycle pc " +
           "JOIN Subscription s ON pc.subscriptionId = s.subscriptionId " +
           "WHERE s.userId = :userId " +
           "AND pc.dueDate BETWEEN :startDate AND :endDate " +
           "AND pc.status = 'PENDING' " +
           "ORDER BY pc.dueDate ASC")
    List<PaymentCycle> findUpcomingPayments(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 사용자의 결제 이력 조회 (특정 기간)
     */
    @Query("SELECT pc FROM PaymentCycle pc " +
           "JOIN Subscription s ON pc.subscriptionId = s.subscriptionId " +
           "WHERE s.userId = :userId " +
           "AND pc.dueDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pc.dueDate DESC")
    List<PaymentCycle> findPaymentHistoryByPeriod(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 구독의 연체된 결제 개수 조회
     */
    long countBySubscriptionIdAndStatus(Long subscriptionId, PaymentStatus status);

    /**
     * 특정 구독의 특정 기간 내 결제 주기 조회 (중복 방지용)
     */
    List<PaymentCycle> findBySubscriptionIdAndDueDateBetween(
        Long subscriptionId, 
        LocalDate startDate, 
        LocalDate endDate
    );

    /**
     * 구독 삭제 전 결제 주기 정리
     */
    void deleteBySubscriptionId(Long subscriptionId);
}
