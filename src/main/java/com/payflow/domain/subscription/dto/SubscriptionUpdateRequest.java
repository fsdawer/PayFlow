package com.payflow.domain.subscription.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.payflow.domain.subscription.entity.Subscription.CycleType;
import com.payflow.domain.subscription.entity.Subscription.Status;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionUpdateRequest {

    // 1. 기본 정보 수정
    private String subscriptionsName;      // 구독 이름 변경
    private String subscriptionsCategory;  // 카테고리 변경
    
    @Min(value = 0, message = "금액은 0 이상이어야 합니다")
    private Integer amount;                // 금액 변경
    private String currency;               // 통화 변경

    // 2. 결제 주기 수정
    private CycleType cycleType;           // 월간 → 연간 변경 등
    private Integer billingDay;            // 결제일 변경 (월간: 1-31)
    private Integer billingWeekday;        // 주간: 0-6 (일-토)
    private Integer billingMonth;          // 연간: 1-12
    private Integer billingDate;           // 연간: 1-31

    // 3. 알림 설정 수정
    private Boolean reminderD3;            // 3일 전 알림 on/off
    private Boolean reminderD1;            // 1일 전 알림 on/off

    // 4. 상태 변경
    private Status status;                 // ACTIVE, PAUSED, CANCELED
    
    // 5. 기타 정보 수정
    @JsonProperty("bankName")
    @JsonAlias("bank_name")
    private String bankName;               // 은행/카드사 변경
    private String memo;                   // 메모 업데이트


}
