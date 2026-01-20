package com.payflow.domain.subscription.entity;

import com.payflow.domain.subscription.dto.SubscriptionUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long subscriptionId;

  @Column(nullable = false)
  private Long userId;

  @Column(name = "subscriptions_name", nullable = false, length = 100)
  private String subscriptionsName;

  @Column(name = "subscriptions_category", length = 50)
  private String subscriptionsCategory;

  @Column(nullable = false)
  private Integer amount;

  @Column(nullable = false, length = 3, columnDefinition = "char(3) default 'KRW'")
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(name = "cycle_type", nullable = false, length = 20)
  private CycleType cycleType;

  @Column(name = "billing_day")
  private Integer billingDay;

  @Column(name = "billing_weekday")
  private Integer billingWeekday;

  @Column(name = "billing_month")
  private Integer billingMonth;

  @Column(name = "billing_date")
  private Integer billingDate;

  @Column(name = "reminder_d3", columnDefinition = "tinyint(1) default 1")
  private Boolean reminderD3;

  @Column(name = "reminder_d1", columnDefinition = "tinyint(1) default 1")
  private Boolean reminderD1;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
  private Status status;

  @Column(name = "bank_name", length = 50)
  private String bankName;  // 은행/카드사 이름

  @Column(length = 255)
  private String memo;

  @Column(nullable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;



  @PrePersist
  void prePersist() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    
    // 기본값 설정
    if (this.currency == null) {
      this.currency = "KRW";
    }
    if (this.reminderD3 == null) {
      this.reminderD3 = true;
    }
    if (this.reminderD1 == null) {
      this.reminderD1 = true;
    }
    if (this.status == null) {
      this.status = Status.ACTIVE;
    }
  }

  @PreUpdate
  void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * 구독 정보 업데이트
   * null이 아닌 필드만 업데이트 (부분 수정 지원)
   */
  public void update(SubscriptionUpdateRequest request) {
    if (request.getSubscriptionsName() != null) {
      this.subscriptionsName = request.getSubscriptionsName();
    }
    if (request.getSubscriptionsCategory() != null) {
      this.subscriptionsCategory = request.getSubscriptionsCategory();
    }
    if (request.getAmount() != null) {
      this.amount = request.getAmount();
    }
    if (request.getCurrency() != null) {
      this.currency = request.getCurrency();
    }
    if (request.getCycleType() != null) {
      this.cycleType = request.getCycleType();
    }
    if (request.getBillingDay() != null) {
      this.billingDay = request.getBillingDay();
    }
    if (request.getBillingWeekday() != null) {
      this.billingWeekday = request.getBillingWeekday();
    }
    if (request.getBillingMonth() != null) {
      this.billingMonth = request.getBillingMonth();
    }
    if (request.getBillingDate() != null) {
      this.billingDate = request.getBillingDate();
    }
    if (request.getReminderD3() != null) {
      this.reminderD3 = request.getReminderD3();
    }
    if (request.getReminderD1() != null) {
      this.reminderD1 = request.getReminderD1();
    }
    if (request.getStatus() != null) {
      this.status = request.getStatus();
    }
    if (request.getBankName() != null) {
      this.bankName = request.getBankName();
    }
    if (request.getMemo() != null) {
      this.memo = request.getMemo();
    }
  }

  public enum CycleType {
    MONTHLY,
    YEARLY,
    WEEKLY
  }

  public enum Status {
    ACTIVE,
    PAUSED,
    CANCELED
  }

}