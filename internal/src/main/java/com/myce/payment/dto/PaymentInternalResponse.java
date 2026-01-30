package com.myce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInternalResponse {
    private Long paymentId;
    private String impUid;
    private String merchantUid;
    private Long reservationId;
}
