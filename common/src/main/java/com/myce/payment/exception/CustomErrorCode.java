package com.myce.payment.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {
    // 결제 P
    PAYMENT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "P001", "유효하지 않은 결제 상태값입니다."),
    PAYMENT_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P003", "결제 내역을 찾을 수 없습니다."),
    PORTONE_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P004", "포트원 결제내역 응답이 없습니다."),
    PAYMENT_NOT_PAID(HttpStatus.CONFLICT, "P005", "결제가 완료되지 않았습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "P006", "결제 금액이 다릅니다."),
    PAYMENT_MERCHANT_UID_MISMATCH(HttpStatus.BAD_REQUEST, "P007", "주문번호가 일치하지 않습니다."),
    PORTONE_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "P008", "포트원 인증에 실패했습니다."),
    INVALID_PAYMENT_TARGET_TYPE(HttpStatus.BAD_REQUEST, "P009", "유효하지 않은 결제 타겟입니다."),
    PORTONE_REFUND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P010", "포트원 환불 요청에 실패했습니다."),
    REFUND_AMOUNT_EXCEEDS_PAID(HttpStatus.BAD_REQUEST, "P011", "환불 금액이 결제 금액을 초과합니다."),
    PORTONE_REQUEST_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P012", "포트원 요청 본문 직렬화에 실패했습니다."),
    PORTONE_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "P013", "포트원 요청에 실패했습니다."),
    PAYMENT_NOT_READY_OR_PAID(HttpStatus.BAD_REQUEST, "P014", "결제 상태가 'ready' 또는 'paid'가 아닙니다."),
    WEBHOOK_DATA_MISMATCH(HttpStatus.BAD_REQUEST, "P015", "웹훅 데이터와 포트원 조회 데이터가 일치하지 않습니다."),
    INVALID_MERCHANT_UID_FORMAT(HttpStatus.BAD_REQUEST, "P016", "유효하지 않은 상점 주문번호 형식입니다."),
    PAYMENT_SESSION_EXPIRED(HttpStatus.BAD_REQUEST, "P017", "결제 세션이 만료되었습니다. 다시 시도해주세요."),

    // 환불 RF
    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND, "RF001", "환불 정보가 존재하지 않습니다."),
    ALREADY_REFUND_REQUESTED(HttpStatus.CONFLICT, "RF002", "이미 환불 신청이 접수되었습니다."),
    REFUND_SEVEN_DAY_RULE_VIOLATION(HttpStatus.BAD_REQUEST, "RF003", "개최 7일 전에는 환불이 불가능합니다."),
    ALREADY_REFUNDED(HttpStatus.NOT_FOUND, "RF001", "이미 환불이 완료된 결제 입니다."),
    REFUND_NOT_ALLOWED(HttpStatus.NOT_ACCEPTABLE, "RF004", "환불이 불가능한 날짜입니다");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}