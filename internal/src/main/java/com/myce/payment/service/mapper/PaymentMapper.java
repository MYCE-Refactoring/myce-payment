package com.myce.payment.service.mapper;

import com.myce.payment.dto.PaymentInternalRequest;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.type.PaymentMethod;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.service.portone.constant.PortOneResponseKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.stereotype.Component;
/**
 * - 포트원 응답(Map) → Payment 엔티티로 변환
 * - core의 PaymentMapper 역할을 payment 서비스 쪽에서 동일하게 수행
 *  ### 핵심 변경점
 *   - 입력 DTO가 다름
 *     PaymentVerifyInfo(core 전용) → PaymentInternalRequest(payment 전용)
 *   - targetType/targetId 우선 사용
 *     누락 시 reservationId 기반으로 폴백
 *   - PaymentInfo 관련 로직 제거
 *     core 전용 정보는 payment에서 알 필요가 없음
 */

@Component
public class PaymentMapper {
    /**
     * 카드/간편결제용 매핑
     * - core의 toEntity()와 동일한 역할
     * - targetType/targetId가 없으면 reservationId로 폴백
     */
    public Payment toEntity(PaymentInternalRequest request, Map<String, Object> portOnePayment) {
        String payMethod = (String) portOnePayment.get(PortOneResponseKey.PAY_METHOD);
        PaymentTargetType targetType = request.getTargetType() != null
                ? request.getTargetType()
                : PaymentTargetType.RESERVATION;
        Long targetId = request.getTargetId() != null
                ? request.getTargetId()
                : request.getReservationId();
        return Payment.builder()
                .targetType(targetType)
                .targetId(targetId)
                // 결제 수단/결제사/식별자 저장
                .paymentMethod(PaymentMethod.getPaymentMethod(payMethod))
                .provider((String) portOnePayment.get(PortOneResponseKey.PG_PROVIDER))
                .merchantUid(request.getMerchantUid())
                .impUid(request.getImpUid())
                // 카드 정보
                .cardCompany((String) portOnePayment.get(PortOneResponseKey.CARD_NAME))
                .cardNumber((String) portOnePayment.get(PortOneResponseKey.CARD_NUMBER))
                // 계좌 정보
                .accountBank((String) portOnePayment.get(PortOneResponseKey.VBANK_NAME))
                .accountNumber((String) portOnePayment.get(PortOneResponseKey.VBANK_NUM))
                // 국가 + 결제 완료 시작
                .country((String) portOnePayment.get(PortOneResponseKey.COUNTY))
                .paidAt(toPaidAtLocalDateTime(portOnePayment.get(PortOneResponseKey.PAID_AT)))
                .build();
    }
    /**
     * 이체/가상계좌용 매핑
     * - core의 toEntityTransfer()와 동일한 역할
     * - 계좌 필드가 카드와 다르기 때문에 분리
     * - targetType/targetId가 없으면 reservationId로 폴백
     */
    public Payment toEntityTransfer(PaymentInternalRequest request, Map<String, Object> portOnePayment) {
        String payMethod = (String) portOnePayment.get(PortOneResponseKey.PAY_METHOD);
        PaymentTargetType targetType = request.getTargetType() != null
                ? request.getTargetType()
                : PaymentTargetType.RESERVATION;
        Long targetId = request.getTargetId() != null
                ? request.getTargetId()
                : request.getReservationId();
        return Payment.builder()
                .targetType(targetType)
                .targetId(targetId)
                .paymentMethod(PaymentMethod.getPaymentMethod(payMethod))
                .provider((String) portOnePayment.get(PortOneResponseKey.PG_PROVIDER))
                .merchantUid(request.getMerchantUid())
                .impUid(request.getImpUid())
                .cardCompany((String) portOnePayment.get(PortOneResponseKey.CARD_NAME))
                .cardNumber((String) portOnePayment.get(PortOneResponseKey.CARD_NUMBER))
                // 이체는 다른 은행 필드를 사용
                .accountBank((String) portOnePayment.get(PortOneResponseKey.BANK_NAME))
                .accountNumber((String) portOnePayment.get(PortOneResponseKey.BANK_CODE))
                .country((String) portOnePayment.get(PortOneResponseKey.COUNTY))
                .paidAt(toPaidAtLocalDateTime(portOnePayment.get(PortOneResponseKey.PAID_AT)))
                .build();
    }

    /**
     * 포트원 paid_at은 Unix timestamp 형식
     * → LocalDateTime으로 변환
     */
    private LocalDateTime toPaidAtLocalDateTime(Object paidAtObj) {
        if (paidAtObj instanceof Integer) {
            return toLocalDateTime(((Integer) paidAtObj).longValue());
        } else if (paidAtObj instanceof Long) {
            return toLocalDateTime((Long) paidAtObj);
        }
        return null;
    }

    // Unix 타임스탬프를 LocalDateTime으로 변환
    private LocalDateTime toLocalDateTime(Long unixTimestamp) {
        if (unixTimestamp == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimestamp), ZoneId.systemDefault());
    }
}
