package com.myce.payment.controller;

import com.myce.payment.dto.PaymentInternalRequest;
import com.myce.payment.dto.PaymentInternalResponse;
import com.myce.payment.service.PaymentInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/payment")
@RequiredArgsConstructor
public class PaymentInternalController {

    private final PaymentInternalService paymentInternalService;
    //payments TODO 행동에 관한 API 왜 안되고 http method CHECk POST사용으로 API 안해도 됨
    @PostMapping
    public ResponseEntity<PaymentInternalResponse> verifyAndSave(@RequestBody PaymentInternalRequest request) {
        return ResponseEntity.ok(paymentInternalService.verifyAndSavePayment(request));
    }

    // 가상계좌용 내부 API
    @PostMapping("/vbank")
    public ResponseEntity<PaymentInternalResponse> verifyAndSaveVbank(
            @RequestBody PaymentInternalRequest request) {

        return ResponseEntity.ok(paymentInternalService.verifyAndSaveVbankPayment(request));
    }
}

