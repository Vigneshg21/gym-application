package com.codexgym.gym.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codexgym.gym.config.NotificationProperties;
import com.codexgym.gym.entity.Invoice;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.NotificationLog;
import com.codexgym.gym.entity.enums.InvoiceStatus;
import com.codexgym.gym.entity.enums.InvoiceType;
import com.codexgym.gym.entity.enums.NotificationStatus;
import com.codexgym.gym.exception.ApiException;
import com.codexgym.gym.messaging.NotificationEventMessage;
import com.codexgym.gym.messaging.NotificationPublisher;
import com.codexgym.gym.repository.NotificationLogRepository;
import com.codexgym.gym.service.InvoiceDocumentFactory;
import com.codexgym.gym.service.NotificationMessageFactory;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private BillingService billingService;

    @Mock
    private MembershipService membershipService;

    @Mock
    private MemberService memberService;

    @Mock
    private MemberCardService memberCardService;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private NotificationPublisher notificationPublisher;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = buildNotificationService("admin-chat");
    }

    @Test
    void queueInvoiceReceiptPublishesReceiptAttachmentForTelegram() {
        when(notificationLogRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(UUID.randomUUID());
            log.setStatus(NotificationStatus.PENDING);
            return log;
        });

        Invoice invoice = buildInvoice(new BigDecimal("0.00"), InvoiceStatus.PAID);
        when(billingService.findInvoiceEntity(invoice.getId())).thenReturn(invoice);

        notificationService.queueInvoiceReceipt(invoice.getId());

        ArgumentCaptor<NotificationEventMessage> messageCaptor = ArgumentCaptor.forClass(NotificationEventMessage.class);
        verify(notificationPublisher, times(2)).publish(messageCaptor.capture());

        assertThat(messageCaptor.getAllValues())
                .allSatisfy(message -> {
                    assertThat(message.getAttachment()).isNotNull();
                    assertThat(message.getAttachment().getFileName()).isEqualTo("INV-20260314-FE219A83-receipt.html");
                    assertThat(message.getAttachment().getContentType()).isEqualTo("text/html");
                });
        assertThat(new String(messageCaptor.getAllValues().get(0).getAttachment().getContent(), StandardCharsets.UTF_8))
                .contains("Payment Receipt")
                .contains("INV-20260314-FE219A83")
                .contains("Aarav Sharma");
    }

    @Test
    void queueInvoiceReceiptRejectsUnpaidInvoice() {
        Invoice invoice = buildInvoice(new BigDecimal("300.00"), InvoiceStatus.PENDING);
        when(billingService.findInvoiceEntity(invoice.getId())).thenReturn(invoice);

        assertThatThrownBy(() -> notificationService.queueInvoiceReceipt(invoice.getId()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Only paid invoices can send a receipt");
    }
    @Test
    void queueInvoiceReceiptSkipsAdminCopyWhenTelegramChatMatchesMember() {
        notificationService = buildNotificationService("member-chat");
        when(notificationLogRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(UUID.randomUUID());
            log.setStatus(NotificationStatus.PENDING);
            return log;
        });

        Invoice invoice = buildInvoice(new BigDecimal("0.00"), InvoiceStatus.PAID);
        when(billingService.findInvoiceEntity(invoice.getId())).thenReturn(invoice);

        notificationService.queueInvoiceReceipt(invoice.getId());

        verify(notificationPublisher, times(1)).publish(any(NotificationEventMessage.class));
    }

    private NotificationService buildNotificationService(String adminChatId) {
        NotificationProperties properties = new NotificationProperties();
        properties.setBrandName("AJEESH HIFI FITNESS");
        properties.getWhatsapp().setEnabled(false);
        properties.getEmail().setEnabled(false);
        properties.getTelegram().setEnabled(true);
        properties.getTelegram().setChatId(adminChatId);

        Clock clock = Clock.fixed(Instant.parse("2026-03-14T04:00:00Z"), ZoneId.of("Asia/Calcutta"));
        return new NotificationService(
                billingService,
                membershipService,
                memberService,
                memberCardService,
                notificationLogRepository,
                new InvoiceDocumentFactory(properties),
                new NotificationMessageFactory(properties),
                notificationPublisher,
                properties,
                clock
        );
    }

    private Invoice buildInvoice(BigDecimal balanceDue, InvoiceStatus status) {
        Member member = new Member();
        member.setId(UUID.randomUUID());
        member.setFirstName("Aarav");
        member.setLastName("Sharma");
        member.setPhoneNumber("+919999999999");
        member.setTelegramChatId("member-chat");

        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setMember(member);
        invoice.setInvoiceNumber("INV-20260314-FE219A83");
        invoice.setIssueDate(LocalDate.of(2026, 3, 14));
        invoice.setDueDate(LocalDate.of(2026, 3, 17));
        invoice.setAmount(new BigDecimal("3000.00"));
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(new BigDecimal("3000.00"));
        invoice.setAmountPaid(new BigDecimal("3000.00").subtract(balanceDue));
        invoice.setBalanceDue(balanceDue);
        invoice.setStatus(status);
        invoice.setType(InvoiceType.MEMBERSHIP_SIGNUP);
        return invoice;
    }
}