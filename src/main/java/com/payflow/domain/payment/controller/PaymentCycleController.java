package com.payflow.domain.payment.controller;

import com.payflow.domain.payment.dto.PaymentCompleteRequest;
import com.payflow.domain.payment.dto.PaymentCycleResponse;
import com.payflow.domain.payment.dto.PaymentHistoryResponse;
import com.payflow.domain.payment.dto.UpcomingPaymentsResponse;
import com.payflow.domain.payment.entity.PaymentCycle;
import com.payflow.domain.payment.repository.PaymentCycleRepository;
import com.payflow.domain.payment.service.PaymentCycleService;
import com.payflow.domain.subscription.entity.Subscription;
import com.payflow.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment-cycles")
@RequiredArgsConstructor
public class PaymentCycleController {

    private final PaymentCycleService paymentCycleService;
    private final PaymentCycleRepository paymentCycleRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 다가오는 결제 조회
     * GET /api/payment-cycles/upcoming?days=7
     * 
     * 사용 예시: 대시보드에서 "앞으로 7일 내 결제 예정" 표시
     */
    @GetMapping("/upcoming")
    public ResponseEntity<UpcomingPaymentsResponse> getUpcomingPayments(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "7") int days
    ) {
        List<PaymentCycle> cycles = paymentCycleService.getUpcomingPayments(userId, days);
        
        // PaymentCycle → PaymentCycleResponse 변환 (구독 정보 포함)
        List<PaymentCycleResponse> responses = cycles.stream()
                .map(cycle -> {
                    Subscription sub = subscriptionRepository.findById(cycle.getSubscriptionId()).orElse(null);
                    return sub != null 
                        ? PaymentCycleResponse.from(cycle, sub) 
                        : PaymentCycleResponse.from(cycle);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(UpcomingPaymentsResponse.from(responses, days));
    }

    /**
     * 결제 이력 조회 (월별)
     * GET /api/payment-cycles/history?year=2026&month=1
     * 
     * 사용 예시: "2026년 1월 지출 분석" 페이지
     */
    @GetMapping("/history")
    public ResponseEntity<PaymentHistoryResponse> getPaymentHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        List<PaymentCycle> cycles = paymentCycleService.getPaymentHistory(userId, startDate, endDate);
        
        // PaymentCycle → PaymentCycleResponse 변환 (구독 정보 포함)
        List<PaymentCycleResponse> responses = cycles.stream()
                .map(cycle -> {
                    Subscription sub = subscriptionRepository.findById(cycle.getSubscriptionId()).orElse(null);
                    return sub != null 
                        ? PaymentCycleResponse.from(cycle, sub) 
                        : PaymentCycleResponse.from(cycle);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(PaymentHistoryResponse.from(responses, startDate, endDate));
    }

    /**
     * 특정 구독의 결제 이력 조회
     * GET /api/payment-cycles/subscription/{subscriptionId}
     * 
     * 사용 예시: 구독 상세 페이지에서 "결제 이력" 탭
     */
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<PaymentCycleResponse>> getPaymentCyclesBySubscription(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long subscriptionId
    ) {
        // 권한 확인: 이 구독이 현재 사용자 것인지
        Subscription subscription = subscriptionRepository.findBySubscriptionIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없거나 권한이 없습니다"));
        
        List<PaymentCycle> cycles = paymentCycleService.getPaymentHistory(
                userId, 
                LocalDate.now().minusYears(1),  // 1년 전부터
                LocalDate.now().plusYears(1)     // 1년 후까지
        ).stream()
         .filter(cycle -> cycle.getSubscriptionId().equals(subscriptionId))
         .collect(Collectors.toList());
        
        List<PaymentCycleResponse> responses = cycles.stream()
                .map(cycle -> PaymentCycleResponse.from(cycle, subscription))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * 결제 완료 처리 (수동)
     * POST /api/payment-cycles/complete
     * 
     * 사용 예시: 할인 받았거나 실제 금액이 다를 때
     * 선택사항: 대부분은 스케줄러가 자동 처리
     */
    @PostMapping("/complete")
    public ResponseEntity<PaymentCycleResponse> markAsComplete(
            @RequestBody PaymentCompleteRequest request
    ) {
        paymentCycleService.markAsPaid(request.getCycleId(), request.getPaidAmount());
        
        PaymentCycle cycle = paymentCycleRepository.findById(request.getCycleId())
                .orElseThrow(() -> new IllegalArgumentException("결제 주기를 찾을 수 없습니다: " + request.getCycleId()));
        
        Subscription sub = subscriptionRepository.findById(cycle.getSubscriptionId()).orElse(null);
        
        return ResponseEntity.ok(sub != null 
            ? PaymentCycleResponse.from(cycle, sub) 
            : PaymentCycleResponse.from(cycle));
    }

    /**
     * 수동으로 결제 주기 생성 (관리자용)
     * POST /api/payment-cycles/generate/{subscriptionId}
     * 
     * 사용 예시: 구독 생성 시 자동 생성 실패했을 때 재시도
     * 일반적으로는 SubscriptionService에서 자동 호출됨
     */
    @PostMapping("/generate/{subscriptionId}")
    public ResponseEntity<String> generatePaymentCycles(
            @PathVariable Long subscriptionId,
            @RequestParam(defaultValue = "12") int monthsAhead
    ) {
        paymentCycleService.createPaymentCyclesForSubscription(subscriptionId, monthsAhead);
        return ResponseEntity.ok("결제 주기 " + monthsAhead + "개월치 생성 완료");
    }
}
