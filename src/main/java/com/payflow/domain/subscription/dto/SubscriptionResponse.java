package com.payflow.domain.subscription.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.payflow.domain.subscription.entity.Subscription.CycleType;
import com.payflow.domain.subscription.entity.Subscription.Status;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {

  private Long subscriptionId;
  private Long userId;
  @JsonProperty("subscriptionsName")
  private String subscriptionsName;
  @JsonProperty("subscriptionsCategory")
  private String subscriptionsCategory;
  private Integer amount;
  private String currency;
  private CycleType cycleType;
  private Integer billingDay;
  private Integer billingWeekday;
  private Integer billingMonth;
  private Integer billingDate;
  private Boolean reminderD3;
  private Boolean reminderD1;
  private Status status;
  @JsonProperty("bankName")
  private String bankName;
  private String memo;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Entity -> DTO 변환 메서드
  public static SubscriptionResponse from(com.payflow.domain.subscription.entity.Subscription subscription) {
    return SubscriptionResponse.builder()
        .subscriptionId(subscription.getSubscriptionId())
        .userId(subscription.getUserId())
        .subscriptionsName(subscription.getSubscriptionsName())
        .subscriptionsCategory(subscription.getSubscriptionsCategory())
        .amount(subscription.getAmount())
        .currency(subscription.getCurrency())
        .cycleType(subscription.getCycleType())
        .billingDay(subscription.getBillingDay())
        .billingWeekday(subscription.getBillingWeekday())
        .billingMonth(subscription.getBillingMonth())
        .billingDate(subscription.getBillingDate())
        .reminderD3(subscription.getReminderD3())
        .reminderD1(subscription.getReminderD1())
        .status(subscription.getStatus())
        .bankName(subscription.getBankName())
        .memo(subscription.getMemo())
        .createdAt(subscription.getCreatedAt())
        .updatedAt(subscription.getUpdatedAt())
        .build();
  }
}
