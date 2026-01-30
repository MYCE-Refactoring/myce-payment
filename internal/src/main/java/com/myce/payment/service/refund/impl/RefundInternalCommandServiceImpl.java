package com.myce.payment.service.refund.impl;

import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.dto.RefundInternalRequest;
import com.myce.payment.dto.RefundInternalResponse;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import com.myce.payment.exception.CustomErrorCode;
import com.myce.payment.exception.CustomException;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import com.myce.payment.service.portone.PortOneApiService;
import com.myce.payment.service.portone.constant.PortOneResponseKey;
import com.myce.payment.service.refund.RefundInternalCommandService;
import com.myce.payment.service.mapper.RefundMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
  - 역할: “상태 변경” 로직만 담당.
  - 흐름: Controller → Facade → Command → PortOne/DB → 응답.
 */
@Service
@RequiredArgsConstructor
public class RefundInternalCommandServiceImpl implements RefundInternalCommandService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PortOneApiService portOneApiService;
    private final RefundMapper refundMapper;

    @Override
    @Transactional
    public RefundInternalResponse refund(RefundInternalRequest request) {
        // 1) 결제 식별 (paymentId > impUid > merchantUid)
        Payment payment = resolvePayment(request);

        // 2) 중복 환불 방지
        ensureNoDuplicateRefund(payment);

        // 3)  PortOne에서 원금 재조회
        Integer originalPaidAmount = fetchOriginalPaidAmount(payment.getImpUid());

        // 4) PortOne 환불 요청 데이터 구성
        PaymentRefundRequest refundRequest = PaymentRefundRequest.builder()
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .cancelAmount(request.getCancelAmount())
                .reason(request.getReason())
                .refundHolder(request.getRefundHolder())
                .refundBank(request.getRefundBank())
                .refundAccount(request.getRefundAccount())
                .refundTel(request.getRefundTel())
                .build();

        // 5) PortOne 환불 호출
        String accessToken = portOneApiService.getAccessToken();
        Map<String, Object> responseBody =
                portOneApiService.requestRefundToPortOne(refundRequest, originalPaidAmount, accessToken);

        // 6) 환불 금액/부분환불 여부 계산
        Integer refundedAmount = toInt(responseBody.get("cancel_amount"));
        boolean isPartial =
                request.getCancelAmount() != null && request.getCancelAmount() < originalPaidAmount;

        // 7) Refund 저장/갱신
        Refund refund = saveOrUpdateRefund(payment, refundedAmount, request.getReason(), isPartial);

        // 8) 응답 반환
        return refundMapper.toResponse(refund, payment);

    }

    @Override
    @Transactional
    public RefundInternalResponse requestRefund(RefundInternalRequest request) {
        // 1) 결제 식별
        Payment payment = resolvePayment(request);

        // 2) 중복 환불 방지
        ensureNoDuplicateRefund(payment);

        // 3) 원금 재조회 후 요청 금액 검증
        Integer originalPaidAmount = fetchOriginalPaidAmount(payment.getImpUid());
        Integer requestedAmount = request.getCancelAmount() != null
                ? request.getCancelAmount()
                : originalPaidAmount;

        if (requestedAmount > originalPaidAmount) {
            throw new CustomException(CustomErrorCode.REFUND_AMOUNT_EXCEEDS_PAID);
        }

        boolean isPartial =
                request.getCancelAmount() != null && requestedAmount < originalPaidAmount;


        // 4) PENDING Refund 생성 (PortOne 호출 없음)
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(requestedAmount)
                .reason(request.getReason())
                .status(RefundStatus.PENDING)
                .isPartial(isPartial)
                .refundedAt(null)
                .build();

        refundRepository.save(refund);
        return refundMapper.toResponse(refund, payment);
    }

    @Override
    @Transactional
    public RefundInternalResponse rejectRefund(RefundInternalRequest request) {
        // 1) 결제 식별
        Payment payment = resolvePayment(request);

        // 2) 환불 조회
        Refund refund = refundRepository.findByPayment(payment)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REFUND_NOT_FOUND));

        // 3) 이미 처리된 경우는 차단 (중복 거절 방지)
        if (refund.getStatus() == RefundStatus.REFUNDED) {
            throw new CustomException(CustomErrorCode.ALREADY_REFUNDED);
        }
        if (refund.getStatus() == RefundStatus.REJECTED) {
            return refundMapper.toResponse(refund, payment);
        }

        // 4) PENDING -> REJECTED
        refund.updateToRejected();
        refundRepository.save(refund);

        return refundMapper.toResponse(refund, payment);
    }

    // 결제 식별자 우선순위: paymentId > impUid > merchantUid
    private Payment resolvePayment(RefundInternalRequest request) {
        if (request.getPaymentId() != null) {
            return paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        }
        if (request.getImpUid() != null) {
            return paymentRepository.findByImpUid(request.getImpUid())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        }
        if (request.getMerchantUid() != null) {
            return paymentRepository.findByMerchantUid(request.getMerchantUid())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        }
        throw new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND);
    }

    // 중복 환불 방지 체크
    private void ensureNoDuplicateRefund(Payment payment) {
        if (refundRepository.existsByPaymentAndStatus(payment, RefundStatus.PENDING)) {
            throw new CustomException(CustomErrorCode.ALREADY_REFUND_REQUESTED);
        }
        if (refundRepository.existsByPaymentAndStatus(payment, RefundStatus.REFUNDED)) {
            throw new CustomException(CustomErrorCode.ALREADY_REFUNDED);
        }
    }

    // PortOne에서 원금 재조회
    private Integer fetchOriginalPaidAmount(String impUid) {
        Map<String, Object> paymentInfo = portOneApiService.getPaymentInfo(impUid);
        return toInt(paymentInfo.get(PortOneResponseKey.AMOUNT));
    }

    // 결제 대상에 따라 Refund 저장 방식 분기
    private Refund saveOrUpdateRefund(Payment payment, Integer refundedAmount, String reason, boolean isPartial) {
        if (payment.getTargetType() == PaymentTargetType.RESERVATION) {
            // 예약 환불은 즉시 완료 상태로 저장
            Refund refund = Refund.builder()
                    .payment(payment)
                    .amount(refundedAmount)
                    .reason(reason)
                    .status(RefundStatus.REFUNDED)
                    .isPartial(isPartial)
                    .refundedAt(LocalDateTime.now())
                    .build();
            return refundRepository.save(refund);
        }

        // 광고/박람회는 기존 PENDING이 있으면 REFUNDED로 갱신
        Optional<Refund> pendingRefund =
                refundRepository.findByPaymentAndStatus(payment, RefundStatus.PENDING);
        if (pendingRefund.isPresent()) {
            Refund refund = pendingRefund.get();
            refund.updateToRefund();
            return refundRepository.save(refund);
        }

        // PENDING이 없으면 fallback으로 신규 생성
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(refundedAmount)
                .reason(reason)
                .status(RefundStatus.REFUNDED)
                .isPartial(isPartial)
                .refundedAt(LocalDateTime.now())
                .build();
        return refundRepository.save(refund);
    }

    // PortOne 숫자 타입 안전 변환
    private Integer toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;

    }
}
