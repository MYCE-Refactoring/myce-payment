package com.myce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInternalRequest {
    private String impUid;        // 포트원 결제 고유번호
    private String merchantUid;   // 가맹점 주문번호
    private Integer amount;       // 결제 금액
    private Long reservationId;   // 예약 ID
}