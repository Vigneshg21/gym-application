package com.codexgym.gym.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codexgym.gym.dto.PaymentResponse;
import com.codexgym.gym.dto.RecordPaymentRequest;
import com.codexgym.gym.entity.Invoice;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.Payment;
import com.codexgym.gym.entity.enums.InvoiceStatus;
import com.codexgym.gym.entity.enums.PaymentMethod;
import com.codexgym.gym.exception.ApiException;
import com.codexgym.gym.repository.InvoiceRepository;
import com.codexgym.gym.repository.PaymentRepository;
import com.codexgym.gym.service.GymMapper;
import com.codexgym.gym.service.InvoiceNumberGenerator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceNumberGenerator invoiceNumberGenerator;

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-14T04:00:00Z"), ZoneId.of("Asia/Calcutta"));
        billingService = new BillingService(invoiceRepository, paymentRepository, invoiceNumberGenerator, new GymMapper(), clock);
    }

    @Test
    void recordPaymentUpdatesInvoiceBalanceAndStatus() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = buildInvoice(invoiceId, new BigDecimal("100.00"), LocalDate.of(2026, 3, 20));

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = billingService.recordPayment(
                invoiceId,
                new RecordPaymentRequest(new BigDecimal("40.00"), PaymentMethod.UPI, "UPI-123", "Front Desk", "Partial payment")
        );

        assertThat(response.amount()).isEqualByComparingTo("40.00");
        assertThat(invoice.getAmountPaid()).isEqualByComparingTo("40.00");
        assertThat(invoice.getBalanceDue()).isEqualByComparingTo("60.00");
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
    }

    @Test
    void recordPaymentRejectsOverpayment() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = buildInvoice(invoiceId, new BigDecimal("30.00"), LocalDate.of(2026, 3, 20));

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> billingService.recordPayment(
                invoiceId,
                new RecordPaymentRequest(new BigDecimal("50.00"), PaymentMethod.CASH, null, "Desk", null)
        )).isInstanceOf(ApiException.class)
                .hasMessageContaining("exceeds the outstanding balance");
    }

    private Invoice buildInvoice(UUID invoiceId, BigDecimal total, LocalDate dueDate) {
        Member member = new Member();
        member.setId(UUID.randomUUID());
        member.setFirstName("Rahul");
        member.setLastName("Verma");
        member.setPhoneNumber("+919999999999");

        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setMember(member);
        invoice.setInvoiceNumber("INV-TEST");
        invoice.setIssueDate(LocalDate.of(2026, 3, 14));
        invoice.setDueDate(dueDate);
        invoice.setAmount(total);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(total);
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceDue(total);
        invoice.setStatus(InvoiceStatus.PENDING);
        return invoice;
    }
}

