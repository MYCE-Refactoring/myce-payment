package com.myce.payment.service.impl;

import com.myce.payment.dto.PaymentInternalDetailResponse;
import com.myce.payment.dto.PaymentInternalRequest;
import com.myce.payment.dto.PaymentInternalResponse;
import com.myce.payment.dto.PaymentInternalTargetRequest;
import com.myce.payment.dto.PaymentWebhookInternalRequest;
import com.myce.payment.dto.PaymentWebhookInternalResponse;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.type.PaymentMethod;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.exception.CustomErrorCode;
import com.myce.payment.exception.CustomException;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.service.PaymentInternalService;
import com.myce.payment.service.mapper.PaymentMapper;
import com.myce.payment.service.portone.PortOneApiService;
import com.myce.payment.service.portone.constant.PortOneResponseKey;
import com.myce.payment.service.portone.constant.PortOneStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentInternalServiceImpl implements PaymentInternalService {

    // Payment 테이블 저장/조회용 Repository
    private final PaymentRepository paymentRepository;
    // PortOne(외부 결제 시스템) 호출용 서비스
    private final PortOneApiService portOneApiService;
    // PortOne 응답(Map) → Payment 엔티티 변환기
    private final PaymentMapper paymentMapper;

    /**
     * 포트원 결제 검증 + Payment 저장
     * 1) PortOne에서 결제 정보 조회
     * 2) 금액/상태/merchantUid 검증
     * 3) Payment 엔티티로 변환 후 DB 저장
     * 4) 저장 결과를 core로 반환
     */
    @Override
    @Transactional
    public PaymentInternalResponse verifyAndSavePayment(PaymentInternalRequest request) {
        // 1) 포트원 결제 정보 조회
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid());

        // 2) 결제 상태/금액/merchantUid 검증
        verifyPaymentDetails(portOnePayment, request.getAmount(), request.getMerchantUid());

        // 3) 결제 수단에 따라 매핑 로직 선택
        String payMethod = (String) portOnePayment.get(PortOneResponseKey.PAY_METHOD);
        Payment payment;
        if (PaymentMethod.CARD.getName().equalsIgnoreCase(payMethod)) {
            payment = paymentMapper.toEntity(request, portOnePayment);
        } else {
            payment = paymentMapper.toEntityTransfer(request, portOnePayment);
        }

        // 4) Payment DB 저장
        paymentRepository.save(payment);

        // 5) 저장 결과 반환 (core가 사용)
        return PaymentInternalResponse.builder()
                .paymentId(payment.getId())
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .reservationId(request.getReservationId())
                .build();
    }

    /**
     * 가상계좌 결제 검증 + Payment 저장
     * 1) PortOne 결제 정보 조회
     * 2) status가 ready 또는 paid인지 확인
     * 3) 금액/merchantUid 일치 검증
     * 4) vbank 정보 포함해서 Payment 저장
     * 5) core로 결과 반환
     */
    @Override
    @Transactional
    public PaymentInternalResponse verifyAndSaveVbankPayment(PaymentInternalRequest request) {
        // 1) 포트원 결제 정보 조회
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid());

        // 2) 가상계좌용 검증 (ready 또는 paid 허용)
        verifyVbankDetails(portOnePayment, request.getAmount(), request.getMerchantUid());

        // 3) Payment 엔티티 저장
        // 가상계좌는 vbank_name/vbank_num 필드를 쓰므로 toEntity 사용
        Payment payment = paymentMapper.toEntity(request, portOnePayment);
        paymentRepository.save(payment);

        // 4) 결과 반환
        return PaymentInternalResponse.builder()
                .paymentId(payment.getId())
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .reservationId(request.getReservationId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentInternalDetailResponse getPaymentByTarget(PaymentTargetType targetType, Long targetId) {
        return paymentRepository.findByTargetIdAndTargetType(targetId, targetType)
                .map(this::toDetailResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentInternalDetailResponse> getPaymentsByTargets(List<PaymentInternalTargetRequest> targets) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }

        Map<PaymentTargetType, List<Long>> groupedTargets = targets.stream()
                .filter(Objects::nonNull)
                .filter(target -> target.getTargetType() != null && target.getTargetId() != null)
                .collect(Collectors.groupingBy(
                        PaymentInternalTargetRequest::getTargetType,
                        Collectors.mapping(PaymentInternalTargetRequest::getTargetId, Collectors.toList())
                ));

        List<Payment> payments = new ArrayList<>();
        for (Map.Entry<PaymentTargetType, List<Long>> entry : groupedTargets.entrySet()) {
            payments.addAll(paymentRepository.findByTargetTypeAndTargetIdIn(entry.getKey(), entry.getValue()));
        }

        return payments.stream()
                .map(this::toDetailResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaymentWebhookInternalResponse processWebhook(PaymentWebhookInternalRequest request) {
        if (request == null || request.getImpUid() == null) {
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND);
        }

        Payment payment = paymentRepository.findByImpUid(request.getImpUid())
                .orElseGet(() -> request.getMerchantUid() == null
                        ? null
                        : paymentRepository.findByMerchantUid(request.getMerchantUid()).orElse(null));

        if (payment == null) {
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND);
        }

        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid());

        String status = (String) portOnePayment.get(PortOneResponseKey.STATUS);
        Integer paidAmount = (Integer) portOnePayment.get(PortOneResponseKey.AMOUNT);
        Long paidAt = toUnixTime(portOnePayment.get(PortOneResponseKey.PAID_AT));

        if (PortOneStatus.PAID.equalsIgnoreCase(status) && paidAt != null && paidAt > 0) {
            payment.updateOnSuccess(toLocalDateTime(paidAt));
        }

        return PaymentWebhookInternalResponse.builder()
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .status(status)
                .paidAmount(paidAmount)
                .paidAt(paidAt)
                .targetType(payment.getTargetType())
                .targetId(payment.getTargetId())
                .build();
    }


    /**
     * 결제 검증 로직 (core의 VerifyPaymentService 로직을 payment로 이동)
     * - 결제 상태가 paid인지
     * - 결제 금액이 요청 금액과 일치하는지
     * - merchantUid(주문번호)가 같은지
     */
    private void verifyPaymentDetails(Map<String, Object> portOnePayment, int amount, String merchantUid) {
        String status = (String) portOnePayment.get(PortOneResponseKey.STATUS);
        Integer paidAmount = (Integer) portOnePayment.get(PortOneResponseKey.AMOUNT);
        String portOneMerchantUid = (String) portOnePayment.get(PortOneResponseKey.MERCHANT_UID);

        if (!PortOneStatus.PAID.equalsIgnoreCase(status)) {
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_PAID);
        }
        if (!paidAmount.equals(amount)) {
            throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (!portOneMerchantUid.equals(merchantUid)) {
            throw new CustomException(CustomErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
        }
    }

    /**
     * 가상계좌 검증 로직
     * - status: ready 또는 paid 허용
     * - 금액과 merchantUid 일치 여부 검증
     */
    private void verifyVbankDetails(Map<String, Object> portOnePayment, int amount, String merchantUid) {
        String status = (String) portOnePayment.get(PortOneResponseKey.STATUS);
        Integer paidAmount = (Integer) portOnePayment.get(PortOneResponseKey.AMOUNT);
        String portOneMerchantUid = (String) portOnePayment.get(PortOneResponseKey.MERCHANT_UID);

        // 가상계좌는 ready 또는 paid만 허용
        if (!PortOneStatus.READY.equalsIgnoreCase(status) && !PortOneStatus.PAID.equalsIgnoreCase(status)) {
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_READY_OR_PAID);
        }
        if (!paidAmount.equals(amount)) {
            throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (!portOneMerchantUid.equals(merchantUid)) {
            throw new CustomException(CustomErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
        }
    }

    private PaymentInternalDetailResponse toDetailResponse(Payment payment) {
        return PaymentInternalDetailResponse.builder()
                .paymentId(payment.getId())
                .targetType(payment.getTargetType())
                .targetId(payment.getTargetId())
                .paymentMethod(payment.getPaymentMethod())
                .provider(payment.getProvider())
                .merchantUid(payment.getMerchantUid())
                .impUid(payment.getImpUid())
                .cardCompany(payment.getCardCompany())
                .cardNumber(payment.getCardNumber())
                .accountBank(payment.getAccountBank())
                .accountNumber(payment.getAccountNumber())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private Long toUnixTime(Object paidAtObj) {
        if (paidAtObj instanceof Integer) {
            return ((Integer) paidAtObj).longValue();
        }
        if (paidAtObj instanceof Long) {
            return (Long) paidAtObj;
        }
        return null;
    }

    private LocalDateTime toLocalDateTime(Long unixTimestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimestamp), ZoneId.systemDefault());
    }


}
