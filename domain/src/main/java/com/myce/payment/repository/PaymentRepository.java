package com.myce.payment.repository;

import com.myce.payment.entity.Payment;
import com.myce.payment.entity.type.PaymentTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByImpUid(String impUid);
    Optional<Payment> findByMerchantUid(String merchantUid);
    Optional<Payment> findByTargetIdAndTargetType(Long targetId, PaymentTargetType targetType);
    List<Payment> findByTargetTypeAndTargetIdIn(PaymentTargetType targetType, List<Long> targetIds);
}
