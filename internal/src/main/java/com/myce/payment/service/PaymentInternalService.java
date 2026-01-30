package com.myce.payment.service;

import com.myce.payment.dto.PaymentInternalDetailResponse;
import com.myce.payment.dto.PaymentInternalRequest;
import com.myce.payment.dto.PaymentInternalResponse;
import com.myce.payment.dto.PaymentInternalTargetRequest;
import com.myce.payment.dto.PaymentWebhookInternalRequest;
import com.myce.payment.dto.PaymentWebhookInternalResponse;
import com.myce.payment.entity.type.PaymentTargetType;
import java.util.List;
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

    PaymentInternalDetailResponse getPaymentByTarget(PaymentTargetType targetType, Long targetId);

    List<PaymentInternalDetailResponse> getPaymentsByTargets(List<PaymentInternalTargetRequest> targets);

    PaymentWebhookInternalResponse processWebhook(PaymentWebhookInternalRequest request);
}
