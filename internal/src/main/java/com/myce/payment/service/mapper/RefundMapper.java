package com.myce.payment.service.mapper;

import com.myce.payment.dto.RefundInternalResponse;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import org.springframework.stereotype.Component;

// - 역할: 환불 응답 변환 로직 중복 제거
@Component
public class RefundMapper {
    // Refund + Payment 정보를 응답 DTO로 변환
    public RefundInternalResponse toResponse(Refund refund, Payment payment) {
        return RefundInternalResponse.builder()
                .refundId(refund.getId())
                .paymentId(payment.getId())
                .targetType(payment.getTargetType())
                .targetId(payment.getTargetId())
                .refundedAmount(refund.getAmount())
                .isPartial(refund.getIsPartial())
                .status(refund.getStatus())
                .refundedAt(refund.getRefundedAt())
                // 조회 화면에 필요한 데이터
                .reason(refund.getReason())
                .requestedAt(refund.getCreatedAt())
                .build();
    }
}
