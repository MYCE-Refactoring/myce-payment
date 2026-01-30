package com.myce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 역할: refunds/sum API의 응답을 구조화(숫자만 던지지 않고 의미를 명확히 함).

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundSumResponse {
    private Long totalAmount;
}


