package com.payflow.domain.payment.service;

import com.payflow.domain.payment.entity.PaymentCycle;
import com.payflow.domain.payment.entity.PaymentCycle.PaymentStatus;
import com.payflow.domain.payment.repository.PaymentCycleRepository;
import com.payflow.domain.subscription.entity.Subscription;
import com.payflow.domain.subscription.entity.Subscription.CycleType;
import com.payflow.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCycleServiceImpl implements PaymentCycleService {

    private final PaymentCycleRepository paymentCycleRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 구독에 대한 결제 주기를 자동 생성
     * 
     * 📌 핵심 로직:
     * 1. 구독 정보 조회 (cycleType, billingDay 등)
     * 2. 결제 주기에 따라 다음 결제일 계산
     *    - MONTHLY: 매월 billingDay 일
     *    - WEEKLY: 매주 billingWeekday 요일
     *    - YEARLY: 매년 billingMonth월 billingDate일
     * 3. 향후 N개월치 PaymentCycle 엔티티 생성 및 저장
     */

    // 구독에 대한 결제 주기를 자동 생성
    @Override
    @Transactional
    public void createPaymentCyclesForSubscription(Long subscriptionId, int monthsAhead) {
        // 1. 구독 정보 조회
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다: " + subscriptionId));

        log.info("결제 주기 생성 시작: subscriptionId={}, cycleType={}, monthsAhead={}", 
                 subscriptionId, subscription.getCycleType(), monthsAhead);

        // 2. 이미 생성된 결제 주기가 있는지 확인 (중복 방지)
        List<PaymentCycle> existingCycles = paymentCycleRepository
            .findBySubscriptionIdAndStatus(subscriptionId, PaymentStatus.PENDING);
        
        if (!existingCycles.isEmpty()) {
            log.warn("이미 생성된 결제 주기가 존재합니다: {} 개", existingCycles.size());
            return;
        }

        // 3. 결제 주기 타입에 따라 PaymentCycle 생성
        List<PaymentCycle> paymentCycles = generatePaymentCycles(subscription, monthsAhead);

        // 4. DB에 저장
        paymentCycleRepository.saveAll(paymentCycles);
        
        log.info("결제 주기 생성 완료: {} 개", paymentCycles.size());
    }

    /**
     * 결제 주기 생성 핵심 로직
     */
    private List<PaymentCycle> generatePaymentCycles(Subscription subscription, int monthsAhead) {
        List<PaymentCycle> cycles = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        CycleType cycleType = subscription.getCycleType();

        switch (cycleType) {
            case MONTHLY -> {
                // 월간 구독: 매월 특정 일자에 결제
                Integer billingDay = subscription.getBillingDay();
                if (billingDay == null) {
                    throw new IllegalStateException("월간 구독의 billingDay가 설정되지 않았습니다");
                }

                for (int i = 0; i < monthsAhead; i++) {
                    LocalDate dueDate = calculateMonthlyDueDate(currentDate, billingDay, i);
                    cycles.add(createPaymentCycle(subscription, dueDate));
                }
            }
            case WEEKLY -> {
                // 주간 구독: 매주 특정 요일에 결제
                Integer billingWeekday = subscription.getBillingWeekday();
                if (billingWeekday == null) {
                    throw new IllegalStateException("주간 구독의 billingWeekday가 설정되지 않았습니다");
                }

                int weeksToGenerate = monthsAhead * 4; // 대략 계산
                for (int i = 0; i < weeksToGenerate; i++) {
                    LocalDate dueDate = calculateWeeklyDueDate(currentDate, billingWeekday, i);
                    cycles.add(createPaymentCycle(subscription, dueDate));
                }
            }
            case YEARLY -> {
                // 연간 구독: 매년 특정 월/일에 결제
                Integer billingMonth = subscription.getBillingMonth();
                Integer billingDate = subscription.getBillingDate();
                
                if (billingMonth == null || billingDate == null) {
                    throw new IllegalStateException("연간 구독의 billingMonth, billingDate가 설정되지 않았습니다");
                }

                int yearsToGenerate = Math.max(1, monthsAhead / 12);
                for (int i = 0; i < yearsToGenerate; i++) {
                    LocalDate dueDate = calculateYearlyDueDate(currentDate, billingMonth, billingDate, i);
                    cycles.add(createPaymentCycle(subscription, dueDate));
                }
            }
        }

        return cycles;
    }

    /**
     * 월간 결제일 계산
     * 예: billingDay=15 -> 매월 15일
     */
    private LocalDate calculateMonthlyDueDate(LocalDate baseDate, int billingDay, int monthOffset) {
        LocalDate targetMonth = baseDate.plusMonths(monthOffset);
        
        // 해당 월의 마지막 날짜를 초과하지 않도록 처리
        int lastDayOfMonth = targetMonth.lengthOfMonth();
        int actualDay = Math.min(billingDay, lastDayOfMonth);
        
        LocalDate dueDate = targetMonth.withDayOfMonth(actualDay);
        
        return dueDate;
    }

    /**
     * 주간 결제일 계산
     * 예: billingWeekday=1 (월요일) -> 매주 월요일
     */
    private LocalDate calculateWeeklyDueDate(LocalDate baseDate, int billingWeekday, int weekOffset) {
        // billingWeekday: 1=월요일, 2=화요일, ..., 7=일요일
        DayOfWeek targetDayOfWeek = DayOfWeek.of(billingWeekday);
        
        LocalDate dueDate = baseDate.plusWeeks(weekOffset)
            .with(TemporalAdjusters.nextOrSame(targetDayOfWeek));
        
        return dueDate;
    }

    /**
     * 연간 결제일 계산
     * 예: billingMonth=3, billingDate=15 -> 매년 3월 15일
     */
    private LocalDate calculateYearlyDueDate(LocalDate baseDate, int billingMonth, int billingDate, int yearOffset) {
        LocalDate targetYear = baseDate.plusYears(yearOffset);
        
        try {
            LocalDate dueDate = LocalDate.of(targetYear.getYear(), billingMonth, billingDate);
            
            // 현재 날짜보다 이전이면 다음 년도로
            if (yearOffset == 0 && dueDate.isBefore(baseDate)) {
                return calculateYearlyDueDate(baseDate, billingMonth, billingDate, 1);
            }
            
            return dueDate;
        } catch (Exception e) {
            // 유효하지 않은 날짜 (예: 2월 30일) 처리
            log.warn("유효하지 않은 연간 결제일: {}월 {}일", billingMonth, billingDate);
            return targetYear.withMonth(billingMonth).with(TemporalAdjusters.lastDayOfMonth());
        }
    }

    /**
     * PaymentCycle 엔티티 생성 헬퍼 메서드
     */
    private PaymentCycle createPaymentCycle(Subscription subscription, LocalDate dueDate) {
        return PaymentCycle.builder()
            .subscriptionId(subscription.getSubscriptionId())
            .dueDate(dueDate)
            .status(PaymentStatus.PENDING)
            .paidAmount(null) // 아직 미결제
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentCycle> getUpcomingPayments(Long userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        return paymentCycleRepository.findUpcomingPayments(userId, today, endDate);
    }

    @Override
    @Transactional
    public void markAsPaid(Long cycleId, Integer paidAmount) {
        PaymentCycle cycle = paymentCycleRepository.findById(cycleId)
            .orElseThrow(() -> new IllegalArgumentException("결제 주기를 찾을 수 없습니다: " + cycleId));
        
        cycle.markAsPaid(paidAmount);
        paymentCycleRepository.save(cycle);
        
        log.info("결제 완료 처리: cycleId={}, amount={}", cycleId, paidAmount);
    }

    @Override
    @Transactional
    public void markAsOverdue(Long cycleId) {
        PaymentCycle cycle = paymentCycleRepository.findById(cycleId)
            .orElseThrow(() -> new IllegalArgumentException("결제 주기를 찾을 수 없습니다: " + cycleId));
        
        cycle.markAsOverdue();
        paymentCycleRepository.save(cycle);
        
        log.warn("연체 처리: cycleId={}, subscriptionId={}", cycleId, cycle.getSubscriptionId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentCycle> getPaymentHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        return paymentCycleRepository.findPaymentHistoryByPeriod(userId, startDate, endDate);
    }
}
