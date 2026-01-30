package com.myce.payment.repository;

import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByPayment(Payment payment);
    Optional<Refund> findByPaymentAndStatus(Payment payment, RefundStatus status);
    boolean existsByPaymentAndStatus(Payment payment, RefundStatus status);
    void deleteByPayment(Payment payment);

    @Query("SELECT SUM(r.amount) FROM Refund r WHERE r.status = 'REFUNDED' " +
            "AND r.payment.targetType = :targetType " +
            "AND r.refundedAt BETWEEN :refundedAtAfter AND :refundedAtBefore")
    Long sumRefundAmountByTypeAndRefundedAtBetween(PaymentTargetType targetType,
                                                   LocalDateTime refundedAtAfter, LocalDateTime refundedAtBefore);
}