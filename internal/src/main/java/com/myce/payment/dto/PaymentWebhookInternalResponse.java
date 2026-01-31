package com.myce.payment.dto;

import com.myce.payment.entity.type.PaymentTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookInternalResponse {
    private String impUid;
    private String merchantUid;
    private String status;
    private Integer paidAmount;
    private Long paidAt;
    private PaymentTargetType targetType;
    private Long targetId;
}
