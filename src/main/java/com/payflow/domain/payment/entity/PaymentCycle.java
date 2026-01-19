package com.payflow.domain.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_cycles")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cycleId;

    @Column(nullable = false)
    private Long subscriptionId;  // int → Long

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;  // Date → LocalDate

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;  // String → Enum

    @Column(name = "paid_amount", nullable = false)
    private Integer paidAmount;  // int → Integer

    @Column(nullable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;


    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }


    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 결제 완료 처리
     */
    public void markAsPaid(Integer amount) {
        this.status = PaymentStatus.PAID;
        this.paidAmount = amount;
    }

    /**
     * 연체 처리
     */
    public void markAsOverdue() {
        this.status = PaymentStatus.OVERDUE;
    }

    /**
     * 취소 처리
     */
    public void markAsCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }

    public enum PaymentStatus {
        PENDING,    // 결제 대기
        PAID,       // 결제 완료
        OVERDUE,    // 연체
        CANCELLED   // 취소됨
    }
}
