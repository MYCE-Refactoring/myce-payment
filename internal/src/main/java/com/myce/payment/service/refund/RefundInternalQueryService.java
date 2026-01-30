package com.myce.payment.service.refund;

import com.myce.payment.dto.RefundInternalResponse;
import com.myce.payment.dto.RefundSumResponse;
import com.myce.payment.entity.type.PaymentTargetType;
import java.time.LocalDate;

/*
  - 역할: “읽기(조회/통계)” 전용.
  - 흐름: Facade → Query → Repository.
 */
public interface RefundInternalQueryService {
    // targetType/targetId로 impUid 조회
    String getImpUid(PaymentTargetType targetType, Long targetId);

    // 환불 상세 조회
    RefundInternalResponse getRefundByTarget(PaymentTargetType targetType, Long targetId);

    // 환불 합계 통계
    RefundSumResponse sumRefundAmount(PaymentTargetType targetType, LocalDate from, LocalDate to);
}
