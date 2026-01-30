package com.myce.payment.service.impl;

import com.myce.payment.dto.RefundInternalRequest;
import com.myce.payment.dto.RefundInternalResponse;
import com.myce.payment.dto.RefundSumResponse;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.service.RefundInternalService;
import com.myce.payment.service.refund.RefundInternalCommandService;
import com.myce.payment.service.refund.RefundInternalQueryService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
/*
  - 역할: 내부 refund 전체 계약을 받지만 “실제 로직은 Command/Query로 분리”.
  - 원래 출처: core에서는 PaymentRefundServiceImpl / RefundRequestServiceImpl / 조회 로직이 각각 따로 있었음. 그걸 internal에서 동일하게 분리.
  - 흐름: Controller → Facade → Command/Query → Repository/PortOne → 응답.
 */
@Service
@RequiredArgsConstructor
public class RefundInternalServiceImpl implements RefundInternalService {

    private final RefundInternalCommandService commandService;
    private final RefundInternalQueryService queryService;

    @Override
    public RefundInternalResponse refund(RefundInternalRequest request) {
        // 즉시 환불(PortOne 호출 포함)은 Command로 위임
        return commandService.refund(request);
    }

    @Override
    public RefundInternalResponse requestRefund(RefundInternalRequest request) {
        // 환불 신청(PENDING 생성)은 Command로 위임
        return commandService.requestRefund(request);
    }

    @Override
    public RefundInternalResponse rejectRefund(RefundInternalRequest request) {
        // 환불 거절(PENDING -> REJECTED)은 Command로 위임
        return commandService.rejectRefund(request);
    }

    @Override
    public String getImpUid(PaymentTargetType targetType, Long targetId) {
        // 조회성 로직은 Query로 위임
        return queryService.getImpUid(targetType, targetId);
    }

    @Override
    public RefundInternalResponse getRefundByTarget(PaymentTargetType targetType, Long targetId) {
        return queryService.getRefundByTarget(targetType, targetId);
    }

    @Override
    public RefundSumResponse sumRefundAmount(PaymentTargetType targetType, LocalDate from, LocalDate to) {
        return queryService.sumRefundAmount(targetType, from, to);
    }
}
