package com.payflow.domain.subscription.service;

import com.payflow.domain.notification.repository.NotificationRepository;
import com.payflow.domain.payment.entity.PaymentCycle;
import com.payflow.domain.payment.repository.PaymentCycleRepository;
import com.payflow.domain.payment.service.PaymentCycleService;
import com.payflow.domain.subscription.dto.SubscriptionCreateRequest;
import com.payflow.domain.subscription.dto.SubscriptionResponse;
import com.payflow.domain.subscription.dto.SubscriptionUpdateRequest;
import com.payflow.domain.subscription.entity.Subscription;
import com.payflow.domain.subscription.repository.SubscriptionRepository;
import com.payflow.domain.user.entity.User;
import com.payflow.domain.user.repository.UserRepository;
import com.payflow.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PaymentCycleService paymentCycleService;
    private final PaymentCycleRepository paymentCycleRepository;
    private final NotificationRepository notificationRepository;
    private final RedisService redisService;


    // 구독 정보 등록
    @Override
    public SubscriptionResponse createSubscription(Long userId, SubscriptionCreateRequest request) {
        // 1. 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다. userId: " + userId);
        }

        log.info("구독 생성 요청: userId={}, name={}, bank={}", userId, request.getSubscriptionsName(), request.getBankName());
        
        // 2. Request → Entity 변환
        Subscription subscription = Subscription.builder()
                .userId(userId)
                .subscriptionsName(request.getSubscriptionsName())
                .subscriptionsCategory(request.getSubscriptionsCategory())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .cycleType(request.getCycleType())
                .billingDay(request.getBillingDay())
                .billingWeekday(request.getBillingWeekday())
                .billingMonth(request.getBillingMonth())
                .billingDate(request.getBillingDate())
                .reminderD3(request.getReminderD3())  // null이면 @PrePersist에서 true 설정
                .reminderD1(request.getReminderD1())  // null이면 @PrePersist에서 true 설정
                .bankName(request.getBankName())
                .memo(request.getMemo())
                .build();

        // 3. 저장
        Subscription saved = subscriptionRepository.save(subscription);

        // 4. 결제 주기 자동 생성 (향후 12개월치)
        try {
            paymentCycleService.createPaymentCyclesForSubscription(saved.getSubscriptionId(), 12);
        } catch (Exception e) {
            // 결제 주기 생성 실패해도 구독 생성은 성공으로 처리
            // 추후 스케줄러로 재생성 가능
            System.err.println("결제 주기 생성 실패: " + e.getMessage());
        }

        // 5. Entity → Response 변환
        return SubscriptionResponse.from(saved);
    }


    // 내 구독 정보 조회
    @Override
    public List<SubscriptionResponse> getSubscriptions(Long userId) {
        // 1. 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다. userId: " + userId);
        }

        // 2. 사용자의 모든 구독 조회
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);

        // 3. Entity List → Response List 변환
        return subscriptions.stream()
                .map(SubscriptionResponse::from)
                .toList();
    }


    // 내 구독 정보 수정
    @Override
    public SubscriptionResponse updateSubscription(Long userId, Long subscriptionId, SubscriptionUpdateRequest request) {
        // 1. 구독 조회 및 권한 확인
        Subscription subscription = subscriptionRepository.findBySubscriptionIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 구독을 찾을 수 없거나 권한이 없습니다. subscriptionId: " + subscriptionId));

        log.info("구독 수정 요청: userId={}, subId={}, bank={}", userId, subscriptionId, request.getBankName());
        
        // updateRequest 파라미터 ->  entity에 업데이트용(update) 메서드 호출
        subscription.update(request);
        
        // 3. 저장
        Subscription updated = subscriptionRepository.save(subscription);

        return SubscriptionResponse.from(updated);
    }


     // 내 구독 정보 삭제
    @Override
    @Transactional
    public void deleteSubscription(Long userId, Long subscriptionId) {
        // 1. 구독 조회 및 권한 확인
        Subscription subscription = subscriptionRepository.findBySubscriptionIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 구독을 찾을 수 없거나 권한이 없습니다. subscriptionId: " + subscriptionId));

        // 2. 연관 결제 주기/알림 정리 (FK 제약 대응)
        List<PaymentCycle> cycles = paymentCycleRepository.findBySubscriptionId(subscriptionId);
        if (!cycles.isEmpty()) {
            List<Long> cycleIds = cycles.stream()
                    .map(PaymentCycle::getCycleId)
                    .toList();
            notificationRepository.deleteByPaymentCycleIdIn(cycleIds);
        }
        paymentCycleRepository.deleteBySubscriptionId(subscriptionId);

        // 2. 삭제 (물리 삭제)
        subscriptionRepository.delete(subscription);
        
        // 또는 논리 삭제를 원한다면:
        // subscription.setStatus(Status.CANCELED);
        // subscriptionRepository.save(subscription);
    }

    @Override
    public SubscriptionResponse findById(Long subscriptionId, Long userId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다: " + subscriptionId));

        // 권한 확인
        if (!subscription.getUserId().equals(userId)) {
            throw new SecurityException("접근 권한이 없습니다");
        }

        // 최근 조회 구독에 추가 (Redis)
        redisService.addRecentSubscription(userId, subscriptionId);

        return SubscriptionResponse.from(subscription);
    }    
}
