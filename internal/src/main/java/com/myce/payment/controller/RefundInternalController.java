package com.myce.payment.controller;

import com.myce.payment.dto.RefundInternalRequest;
import com.myce.payment.dto.RefundInternalResponse;
import com.myce.payment.dto.RefundSumResponse;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.service.RefundInternalService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/payment")
@RequiredArgsConstructor
public class RefundInternalController {

    private final RefundInternalService refundInternalService;

    // 즉시 환불 (PortOne 호출 포함)
    @PostMapping("/refund")
    public ResponseEntity<RefundInternalResponse> refund(
            @RequestBody RefundInternalRequest request) {
        return ResponseEntity.ok(refundInternalService.refund(request));
    }

    // 환불 신청 (PENDING 생성)
    @PostMapping("/refund-request")
    public ResponseEntity<RefundInternalResponse> refundRequest(
            @RequestBody RefundInternalRequest request) {
        return ResponseEntity.ok(refundInternalService.requestRefund(request));
    }

    // 환불 거절 (PENDING -> REJECTED)
    @PostMapping("/refund-reject")
    public ResponseEntity<RefundInternalResponse> refundReject(
            @RequestBody RefundInternalRequest request) {
        return ResponseEntity.ok(refundInternalService.rejectRefund(request));
    }

    // targetType/targetId → impUid 조회
    @GetMapping("/imp-uid")
    public ResponseEntity<String> getImpUid(
            @RequestParam PaymentTargetType targetType,
            @RequestParam Long targetId) {
        return ResponseEntity.ok(refundInternalService.getImpUid(targetType, targetId));
    }

    // 환불 상세 조회
    @GetMapping("/refunds/by-target")
    public ResponseEntity<RefundInternalResponse> getRefundByTarget(
            @RequestParam PaymentTargetType targetType,
            @RequestParam Long targetId) {
        return ResponseEntity.ok(refundInternalService.getRefundByTarget(targetType, targetId));
    }

    // 환불 합계 통계 -> 관리자용
    @GetMapping("/refunds/sum")
    public ResponseEntity<RefundSumResponse> getRefundSum(
            @RequestParam PaymentTargetType targetType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(refundInternalService.sumRefundAmount(targetType, from, to));
    }
}
