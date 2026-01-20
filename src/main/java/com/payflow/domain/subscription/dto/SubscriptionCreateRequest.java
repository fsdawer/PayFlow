package com.payflow.domain.subscription.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.payflow.domain.subscription.entity.Subscription.CycleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SubscriptionCreateRequest {

  @NotBlank(message = "구독 서비스 이름은 필수입니다")
  private String subscriptionsName;   // 구독 이름

  private String subscriptionsCategory;  // 구독 카테고리

  @NotNull(message = "금액은 필수입니다")
  @Min(value = 0, message = "금액은 0 이상이어야 합니다")
  private Integer amount;  // 월 금액

  private String currency; // 기본값: KRW

  @NotNull(message = "결제 주기는 필수입니다")
  private CycleType cycleType;

  // 결제일 관련 필드 (주기에 따라 선택적으로 사용)
  private Integer billingDay;      // 월간: 1-31
  private Integer billingWeekday;  // 주간: 0-6 (일-토)
  private Integer billingMonth;    // 연간: 1-12
  private Integer billingDate;     // 연간: 1-31

  // 알림 설정
  private Boolean reminderD3;      // 기본값: true
  private Boolean reminderD1;      // 기본값: true

  // 은행/카드사
  @JsonProperty("bankName")
  @JsonAlias("bank_name")
  private String bankName;

  // 메모
  private String memo;
}
