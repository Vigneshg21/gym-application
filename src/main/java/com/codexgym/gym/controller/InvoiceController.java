package com.codexgym.gym.controller;

import com.codexgym.gym.application.BillingService;
import com.codexgym.gym.application.NotificationService;
import com.codexgym.gym.dto.InvoiceResponse;
import com.codexgym.gym.dto.PaymentResponse;
import com.codexgym.gym.dto.RecordPaymentRequest;
import jakarta.validation.Valid;
import com.codexgym.gym.exception.ApiException;
import com.codexgym.gym.messaging.NotificationAttachment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Invoices", description = "Invoice and billing management APIs")
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final BillingService billingService;
    private final NotificationService notificationService;

    @Operation(summary = "List all invoices", description = "Retrieves a list of all invoices")
    @GetMapping
    public List<InvoiceResponse> listInvoices() {
        return billingService.listInvoices();
    }

    @Operation(summary = "Record payment", description = "Records a payment for an invoice")
    @PostMapping("/{invoiceId}/payments")
    public ResponseEntity<PaymentResponse> recordPayment(
            @Parameter(description = "Invoice ID") @PathVariable UUID invoiceId,
            @Valid @RequestBody RecordPaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.recordPayment(invoiceId, request));
    }

    @Operation(summary = "List invoice payments", description = "Retrieves all payments for a specific invoice")
    @GetMapping("/{invoiceId}/payments")
    public List<PaymentResponse> listPayments(@Parameter(description = "Invoice ID") @PathVariable UUID invoiceId) {
        return billingService.listPaymentsForInvoice(invoiceId);
    }

    @Operation(summary = "Send receipt notification", description = "Queues receipt notification for delivery")
    @PostMapping("/{invoiceId}/receipt-notifications")
    public ResponseEntity<Map<String, String>> sendReceipt(@Parameter(description = "Invoice ID") @PathVariable UUID invoiceId) {
        notificationService.queueInvoiceReceipt(invoiceId);
        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }

    @Operation(summary = "Send receipt with PDF", description = "Sends receipt notification with attached PDF document")
    @PostMapping(value = "/{invoiceId}/receipt-notifications/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> sendReceiptPdf(
            @Parameter(description = "Invoice ID") @PathVariable UUID invoiceId,
            @Parameter(description = "Receipt PDF file") @RequestParam("receipt") MultipartFile receipt
    ) {
        if (receipt == null || receipt.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Receipt file is required");
        }

        String expectedFilename = "receipt-" + invoiceId + ".pdf";
        String original = receipt.getOriginalFilename();
        if (!expectedFilename.equals(original)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Receipt filename must be " + expectedFilename);
        }

        String contentType = receipt.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("pdf")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Receipt must be a PDF");
        }

        byte[] content;
        try {
            content = receipt.getBytes();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Could not read receipt file");
        }

        NotificationAttachment attachment = new NotificationAttachment(original, contentType, content);
        notificationService.queueInvoiceReceiptPdf(invoiceId, attachment);
        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }
}