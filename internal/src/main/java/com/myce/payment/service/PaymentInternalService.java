package com.myce.payment.service;

import com.myce.payment.dto.PaymentInternalRequest;
import com.myce.payment.dto.PaymentInternalResponse;
/**
 * 내부 전용 결제 서비스 계약.
 * - core가 payment 서버에 결제 검증/저장을 요청할 때 사용
 * - 외부 공개 API가 아님 (internal 전용)
 */

public interface PaymentInternalService {
    /**
     * 포트원 결제 검증 + Payment 저장을 수행하고, 저장 결과를 반환.
     * - Reservation 흐름 MVP 기준 (reservationId 기반)
     */

    PaymentInternalResponse verifyAndSavePayment(PaymentInternalRequest request);


    PaymentInternalResponse verifyAndSaveVbankPayment(PaymentInternalRequest request);
}
