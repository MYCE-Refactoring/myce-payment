package com.myce.payment.service.refund;

import com.myce.payment.dto.RefundInternalRequest;
import com.myce.payment.dto.RefundInternalResponse;

/*
    - 역할: “쓰기(상태 변경)” 전용.
    - 흐름: Facade → Command → PortOne/DB 저장.
 */
public interface RefundInternalCommandService {
    // 즉시 환불 -> Portone 호출 포함
    RefundInternalResponse refund(RefundInternalRequest request);
    // 환불 신청 (PENDING 생성)
    RefundInternalResponse requestRefund(RefundInternalRequest request);
    // 환불 거절 (PENDING -> REJECTED)
    RefundInternalResponse rejectRefund(RefundInternalRequest request);
}
