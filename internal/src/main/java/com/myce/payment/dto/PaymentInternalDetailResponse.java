package com.myce.payment.dto;

import com.myce.payment.entity.type.PaymentMethod;
import com.myce.payment.entity.type.PaymentTargetType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInternalDetailResponse {
    private Long paymentId;
    private PaymentTargetType targetType;
    private Long targetId;
    private PaymentMethod paymentMethod;
    private String provider;
    private String merchantUid;
    private String impUid;
    private String cardCompany;
    private String cardNumber;
    private String accountBank;
    private String accountNumber;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
