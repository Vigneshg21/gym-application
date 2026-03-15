package com.codexgym.gym.application;

import com.codexgym.gym.dto.InvoiceResponse;
import com.codexgym.gym.dto.PaymentResponse;
import com.codexgym.gym.dto.RecordPaymentRequest;
import com.codexgym.gym.entity.Invoice;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.Membership;
import com.codexgym.gym.entity.Payment;
import com.codexgym.gym.entity.enums.InvoiceStatus;
import com.codexgym.gym.entity.enums.InvoiceType;
import com.codexgym.gym.exception.ApiException;
import com.codexgym.gym.repository.InvoiceRepository;
import com.codexgym.gym.repository.PaymentRepository;
import com.codexgym.gym.service.GymMapper;
import com.codexgym.gym.service.InvoiceNumberGenerator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingService {

    private static final List<InvoiceStatus> OPEN_STATUSES = List.of(
            InvoiceStatus.PENDING,
            InvoiceStatus.PARTIALLY_PAID,
            InvoiceStatus.OVERDUE
    );

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final GymMapper gymMapper;
    private final Clock clock;

    @Transactional
    public Invoice createInvoice(
            Member member,
            Membership membership,
            InvoiceType type,
            BigDecimal amount,
            BigDecimal taxAmount,
            BigDecimal discountAmount,
            LocalDate issueDate,
            LocalDate dueDate,
            String notes
    ) {
        BigDecimal safeAmount = sanitizeAmount(amount);
        BigDecimal safeTax = sanitizeAmount(taxAmount);
        BigDecimal safeDiscount = sanitizeAmount(discountAmount);
        BigDecimal total = safeAmount.add(safeTax).subtract(safeDiscount);

        if (total.signum() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invoice total cannot be negative");
        }

        Invoice invoice = new Invoice();
        invoice.setMember(member);
        invoice.setMembership(membership);
        invoice.setInvoiceNumber(invoiceNumberGenerator.nextInvoiceNumber());
        invoice.setIssueDate(issueDate);
        invoice.setDueDate(dueDate == null ? issueDate : dueDate);
        invoice.setAmount(safeAmount);
        invoice.setTaxAmount(safeTax);
        invoice.setDiscountAmount(safeDiscount);
        invoice.setTotalAmount(total);
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceDue(total);
        invoice.setStatus(total.signum() == 0 ? InvoiceStatus.PAID : InvoiceStatus.PENDING);
        invoice.setType(type);
        invoice.setNotes(notes);
        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listInvoices() {
        return invoiceRepository.findAll(Sort.by(Sort.Direction.ASC, "dueDate")).stream()
                .map(gymMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listPaymentsForInvoice(UUID invoiceId) {
        findInvoiceEntity(invoiceId);
        return paymentRepository.findAllByInvoiceIdOrderByPaymentTimestampDesc(invoiceId).stream()
                .map(gymMapper::toResponse)
                .toList();
    }

    @Transactional
    public PaymentResponse recordPayment(UUID invoiceId, RecordPaymentRequest request) {
        Invoice invoice = findInvoiceEntity(invoiceId);
        ensurePayable(invoice, request.amount());

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(request.amount());
        payment.setPaymentTimestamp(LocalDateTime.now(clock));
        payment.setMethod(request.method());
        payment.setReferenceNumber(blankToNull(request.referenceNumber()));
        payment.setCollectedBy(blankToNull(request.collectedBy()));
        payment.setNotes(blankToNull(request.notes()));
        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal newAmountPaid = invoice.getAmountPaid().add(request.amount());
        BigDecimal newBalance = invoice.getTotalAmount().subtract(newAmountPaid);
        invoice.setAmountPaid(newAmountPaid);
        invoice.setBalanceDue(newBalance);
        invoice.setStatus(resolveStatus(invoice.getDueDate(), newBalance));
        invoiceRepository.save(invoice);

        return gymMapper.toResponse(savedPayment);
    }

    @Transactional
    public void refreshOverdueInvoices() {
        LocalDate today = LocalDate.now(clock);
        List<Invoice> invoices = invoiceRepository.findAllByStatusInAndDueDateLessThanEqualOrderByDueDateAsc(
                List.of(InvoiceStatus.PENDING, InvoiceStatus.PARTIALLY_PAID),
                today.minusDays(1)
        );

        invoices.forEach(invoice -> invoice.setStatus(InvoiceStatus.OVERDUE));
        if (!invoices.isEmpty()) {
            invoiceRepository.saveAll(invoices);
        }
    }

    @Transactional(readOnly = true)
    public List<Invoice> findInvoicesDueOnOrBefore(LocalDate dueDate) {
        return invoiceRepository.findAllByStatusInAndDueDateLessThanEqualOrderByDueDateAsc(OPEN_STATUSES, dueDate);
    }

    @Transactional(readOnly = true)
    public Invoice findInvoiceEntity(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invoice not found"));
    }

    @Transactional(readOnly = true)
    public long countPendingInvoices() {
        return invoiceRepository.countByStatus(InvoiceStatus.PENDING)
                + invoiceRepository.countByStatus(InvoiceStatus.PARTIALLY_PAID);
    }

    @Transactional(readOnly = true)
    public long countOverdueInvoices() {
        return invoiceRepository.countByStatus(InvoiceStatus.OVERDUE);
    }

    @Transactional(readOnly = true)
    public BigDecimal outstandingReceivables() {
        return invoiceRepository.findAllByStatusInOrderByDueDateAsc(OPEN_STATUSES).stream()
                .map(Invoice::getBalanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void ensurePayable(Invoice invoice, BigDecimal amount) {
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cancelled invoices cannot accept payments");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invoice is already fully paid");
        }
        if (amount.compareTo(invoice.getBalanceDue()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Payment amount exceeds the outstanding balance");
        }
    }

    private InvoiceStatus resolveStatus(LocalDate dueDate, BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            return InvoiceStatus.PAID;
        }
        if (dueDate.isBefore(LocalDate.now(clock))) {
            return InvoiceStatus.OVERDUE;
        }
        return InvoiceStatus.PARTIALLY_PAID;
    }

    private BigDecimal sanitizeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

