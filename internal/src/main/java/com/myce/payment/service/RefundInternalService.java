package com.myce.payment.service;

import com.myce.payment.dto.RefundInternalRequest;
import com.myce.payment.dto.RefundInternalResponse;
import com.myce.payment.dto.RefundSumResponse;
import com.myce.payment.entity.type.PaymentTargetType;
import java.time.LocalDate;

public interface RefundInternalService {
    RefundInternalResponse refund(RefundInternalRequest request);
    RefundInternalResponse requestRefund(RefundInternalRequest request); // PENDING 생성 흐름
    RefundInternalResponse rejectRefund(RefundInternalRequest request); // PENDING 거절 흐름
    String getImpUid(PaymentTargetType targetType, Long targetId);
    RefundInternalResponse getRefundByTarget(PaymentTargetType targetType, Long targetId); // 환술 영수증/히스토리 조회에 쓰이는 Refund 조회 흐름
    RefundSumResponse sumRefundAmount(PaymentTargetType targetType, LocalDate from, LocalDate to); // 통계 합계
    }
