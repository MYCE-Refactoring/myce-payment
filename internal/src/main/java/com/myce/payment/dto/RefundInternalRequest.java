package com.myce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundInternalRequest {
    private Long paymentId;
    private String impUid;
    private String merchantUid;

    private Integer cancelAmount; // null이면 전액환불

    private String reason;
    // 가상계좌 환불
    private String refundHolder;
    private String refundBank;
    private String refundAccount;
    private String refundTel;
}
