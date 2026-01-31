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
public class PaymentInternalTargetRequest {
    private PaymentTargetType targetType;
    private Long targetId;
}
