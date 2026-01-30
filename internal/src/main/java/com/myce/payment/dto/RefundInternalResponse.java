package com.myce.payment.dto;

import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundInternalResponse {
    private Long refundId; //payment 내부에 환불 저장이 되었는지 확인 가능한 식별자
    private Long paymentId;  // core에서 PaymentInfo/Reservation/Expo/Ad와 매핑

    private PaymentTargetType targetType; // core가 어떤 도메인에 후처리해야 하는지 결정.
    private Long targetId;

    private Integer refundedAmount;  // 실제 환불액
    private Boolean isPartial; // core가 PaymentInfo 상태를 PARTIAL_REFUNDED로 바꿀지 판단.

    private RefundStatus status;  // 화면, 정산용
    private LocalDateTime refundedAt;

    // 조회용 추가 필드
    private String reason;             // 환불 사유
    private LocalDateTime requestedAt; // 환불 요청 시각 (Refund.createdAt)

}
