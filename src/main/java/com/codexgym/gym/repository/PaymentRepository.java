package com.codexgym.gym.repository;

import com.codexgym.gym.entity.Payment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByInvoiceIdOrderByPaymentTimestampDesc(UUID invoiceId);

    @Query("""
            select coalesce(sum(p.amount), 0)
            from Payment p
            where p.paymentTimestamp between :start and :end
            """)
    java.math.BigDecimal sumPaymentsBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}

