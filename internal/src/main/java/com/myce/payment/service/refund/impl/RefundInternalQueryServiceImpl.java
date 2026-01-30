package com.myce.payment.service.refund.impl;

import com.myce.payment.dto.RefundInternalResponse;
import com.myce.payment.dto.RefundSumResponse;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.exception.CustomErrorCode;
import com.myce.payment.exception.CustomException;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import com.myce.payment.service.mapper.RefundMapper;
import com.myce.payment.service.refund.RefundInternalQueryService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
  - 역할: 조회 전용.
  - 흐름: Controller → Facade → Query → Repository → DTO.
 */
@Service
@RequiredArgsConstructor
public class RefundInternalQueryServiceImpl implements RefundInternalQueryService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final RefundMapper refundMapper;

    @Override
    @Transactional(readOnly = true)
    public String getImpUid(PaymentTargetType targetType, Long targetId) {
        // 결제 정보에서 impUid 조회
        return paymentRepository.findByTargetIdAndTargetType(targetId, targetType)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND))
                .getImpUid();
    }

    @Override
    @Transactional(readOnly = true)
    public RefundInternalResponse getRefundByTarget(PaymentTargetType targetType, Long targetId) {
        // targetType/targetId로 Payment 조회
        Payment payment = paymentRepository.findByTargetIdAndTargetType(targetId, targetType)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        // Payment에 매핑된 Refund 조회
        Refund refund = refundRepository.findByPayment(payment)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REFUND_NOT_FOUND));

        return refundMapper.toResponse(refund, payment);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundSumResponse sumRefundAmount(PaymentTargetType targetType, LocalDate from, LocalDate to) {
        // 날짜 범위를 하루 시작~끝으로 확장
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay().minusNanos(1);

        Long total = refundRepository.sumRefundAmountByTypeAndRefundedAtBetween(
                targetType, fromDateTime, toDateTime);

        return RefundSumResponse.builder()
                .totalAmount(total == null ? 0L : total)
                .build();
    }
}
