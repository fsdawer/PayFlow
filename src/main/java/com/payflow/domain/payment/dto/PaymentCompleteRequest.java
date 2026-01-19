package com.payflow.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 완료 요청 DTO
 * 실제 결제가 완료되었을 때 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompleteRequest {

    private Long cycleId;
    
    /**
     * 실제 결제된 금액
     * nullable: 구독 금액과 동일하면 null 가능
     */
    private Integer paidAmount;

    /**
     * 결제 방법 (선택사항)
     * 예: "카드", "계좌이체", "간편결제" 등
     */
    private String paymentMethod;

    /**
     * 결제 메모 (선택사항)
     */
    private String memo;
}
