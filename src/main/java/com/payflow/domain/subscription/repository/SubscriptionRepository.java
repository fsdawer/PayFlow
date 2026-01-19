package com.payflow.domain.subscription.repository;

import com.payflow.domain.subscription.entity.Subscription;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // 사용자별 목록 조회
  List<Subscription> findByUserId(Long userId);

    // 사용자별 상태별 목록 조회
    List<Subscription> findByUserIdAndStatus(Long userId, Subscription.Status status);

    // 구독 ID와 사용자 ID로 조회
    Optional<Subscription> findBySubscriptionIdAndUserId(Long subscriptionId, Long userId);
}
