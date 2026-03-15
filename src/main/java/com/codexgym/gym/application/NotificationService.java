package com.codexgym.gym.application;

import com.codexgym.gym.config.NotificationProperties;
import com.codexgym.gym.entity.Invoice;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.Membership;
import com.codexgym.gym.entity.NotificationLog;
import com.codexgym.gym.entity.enums.NotificationChannel;
import com.codexgym.gym.entity.enums.NotificationEventType;
import com.codexgym.gym.entity.enums.NotificationStatus;
import com.codexgym.gym.exception.ApiException;
import com.codexgym.gym.messaging.NotificationAttachment;
import com.codexgym.gym.messaging.NotificationEventMessage;
import com.codexgym.gym.messaging.NotificationPublisher;
import com.codexgym.gym.repository.NotificationLogRepository;
import com.codexgym.gym.service.InvoiceDocumentFactory;
import com.codexgym.gym.service.NotificationMessageFactory;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final BillingService billingService;
    private final MembershipService membershipService;
    private final MemberService memberService;
    private final MemberCardService memberCardService;
    private final NotificationLogRepository notificationLogRepository;
    private final InvoiceDocumentFactory invoiceDocumentFactory;
    private final NotificationMessageFactory notificationMessageFactory;
    private final NotificationPublisher notificationPublisher;
    private final NotificationProperties notificationProperties;
    private final Clock clock;

    @Transactional
    public void queueInvoiceReminder(UUID invoiceId, NotificationEventType eventType) {
        Invoice invoice = billingService.findInvoiceEntity(invoiceId);
        Member member = invoice.getMember();

        String subject = switch (eventType) {
            case FEE_OVERDUE -> notificationProperties.getBrandName() + " overdue fee reminder";
            case FEE_REMINDER -> notificationProperties.getBrandName() + " fee reminder";
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported invoice notification type");
        };

        String messageBody = switch (eventType) {
            case FEE_OVERDUE -> notificationMessageFactory.buildOverdueReminder(member, invoice);
            case FEE_REMINDER -> notificationMessageFactory.buildInvoiceReminder(member, invoice);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported invoice notification type");
        };

        int queued = 0;
        queued += queueMemberWhatsApp(member, invoice, invoice.getMembership(), eventType, subject, messageBody);
        queued += queueMemberEmail(member, invoice, invoice.getMembership(), eventType, subject, messageBody);
        queued += queueMemberTelegram(member, invoice, invoice.getMembership(), eventType, subject, messageBody);
        queued += queueAdminTelegramCopy(member, invoice, invoice.getMembership(), eventType, subject, messageBody);
        ensureQueued(queued, "No email, Telegram, or WhatsApp recipient is configured for this reminder");
    }

    @Transactional
    public void queueInvoiceReceipt(UUID invoiceId) {
        Invoice invoice = billingService.findInvoiceEntity(invoiceId);
        if (invoice.getBalanceDue().signum() > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only paid invoices can send a receipt");
        }

        Member member = invoice.getMember();
        String subject = notificationProperties.getBrandName() + " payment receipt";
        String messageBody = notificationMessageFactory.buildPaymentReceipt(member, invoice);
        NotificationAttachment receiptAttachment = invoiceDocumentFactory.buildReceiptDocument(invoice);

        int memberQueued = 0;
        memberQueued += queueMemberWhatsApp(member, invoice, invoice.getMembership(), NotificationEventType.PAYMENT_RECEIPT, subject, messageBody);
        memberQueued += queueMemberEmail(member, invoice, invoice.getMembership(), NotificationEventType.PAYMENT_RECEIPT, subject, messageBody, receiptAttachment);
        memberQueued += queueMemberTelegram(member, invoice, invoice.getMembership(), NotificationEventType.PAYMENT_RECEIPT, subject, messageBody, receiptAttachment);
        ensureQueued(memberQueued, "No email, Telegram, or WhatsApp recipient is configured for this receipt");
        queueAdminTelegramCopy(member, invoice, invoice.getMembership(), NotificationEventType.PAYMENT_RECEIPT, subject, messageBody, receiptAttachment);
    }

    @Transactional
    public void queueMemberCard(UUID memberId) {
        queueMemberCardSnapshot(memberCardService.prepareMemberCard(memberId));
    }

    @Transactional
    public void queueMembershipCard(UUID membershipId) {
        queueMemberCardSnapshot(memberCardService.prepareMembershipCard(membershipId));
    }

    @Transactional
    public void queueRenewalReminder(UUID membershipId) {
        Membership membership = membershipService.findMembershipEntity(membershipId);
        Member member = membership.getMember();
        String subject = notificationProperties.getBrandName() + " membership renewal reminder";
        String messageBody = notificationMessageFactory.buildRenewalReminder(member, membership);

        int queued = 0;
        queued += queueMemberWhatsApp(member, null, membership, NotificationEventType.MEMBERSHIP_RENEWAL_REMINDER, subject, messageBody);
        queued += queueMemberEmail(member, null, membership, NotificationEventType.MEMBERSHIP_RENEWAL_REMINDER, subject, messageBody);
        queued += queueMemberTelegram(member, null, membership, NotificationEventType.MEMBERSHIP_RENEWAL_REMINDER, subject, messageBody);
        queued += queueAdminTelegramCopy(member, null, membership, NotificationEventType.MEMBERSHIP_RENEWAL_REMINDER, subject, messageBody);
        ensureQueued(queued, "No email, Telegram, or WhatsApp recipient is configured for this renewal reminder");
    }

    @Transactional
    public void sendAnnouncement(UUID memberId, String messageBody) {
        Member member = memberService.findMemberEntity(memberId);
        String subject = notificationProperties.getBrandName() + " announcement";
        String trimmedMessage = messageBody.trim();

        int queued = 0;
        queued += queueMemberWhatsApp(member, null, null, NotificationEventType.ANNOUNCEMENT, subject, trimmedMessage);
        queued += queueMemberEmail(member, null, null, NotificationEventType.ANNOUNCEMENT, subject, trimmedMessage);
        queued += queueMemberTelegram(member, null, null, NotificationEventType.ANNOUNCEMENT, subject, trimmedMessage);
        queued += queueAdminTelegramCopy(member, null, null, NotificationEventType.ANNOUNCEMENT, subject, trimmedMessage);
        ensureQueued(queued, "No email, Telegram, or WhatsApp recipient is configured for this announcement");
    }

    @Transactional
    public void sendAdminTest(String messageBody) {
        String subject = notificationProperties.getBrandName() + " admin channel test";
        String trimmedMessage = messageBody.trim();
        int queued = 0;

        if (notificationProperties.getEmail().isEnabled() && hasText(notificationProperties.getEmail().getAdminRecipient())) {
            queued += createAndPublishLog(
                    null,
                    null,
                    null,
                    NotificationEventType.ANNOUNCEMENT,
                    NotificationChannel.EMAIL,
                    notificationProperties.getEmail().getAdminRecipient().trim(),
                    subject,
                    trimmedMessage
            );
        }

        if (notificationProperties.getTelegram().isEnabled() && hasText(notificationProperties.getTelegram().getChatId())) {
            queued += createAndPublishLog(
                    null,
                    null,
                    null,
                    NotificationEventType.ANNOUNCEMENT,
                    NotificationChannel.TELEGRAM,
                    notificationProperties.getTelegram().getChatId().trim(),
                    subject,
                    "Admin test message from " + notificationProperties.getBrandName() + ":\n\n" + trimmedMessage
            );
        }

        ensureQueued(queued, "Admin email or Telegram chat is not configured");
    }

    @Transactional
    public void markDelivered(UUID logId) {
        NotificationLog notificationLog = findLog(logId);
        notificationLog.setStatus(NotificationStatus.SENT);
        notificationLog.setSentAt(LocalDateTime.now(clock));
        notificationLog.setErrorMessage(null);
        notificationLogRepository.save(notificationLog);
    }

    @Transactional
    public void markFailed(UUID logId, String errorMessage) {
        NotificationLog notificationLog = findLog(logId);
        notificationLog.setStatus(NotificationStatus.FAILED);
        notificationLog.setErrorMessage(errorMessage);
        notificationLogRepository.save(notificationLog);
    }

    @Transactional(readOnly = true)
    public boolean wasInvoiceReminderSentToday(UUID invoiceId, NotificationEventType eventType) {
        return notificationLogRepository.existsByInvoiceIdAndEventTypeAndCreatedAtBetween(
                invoiceId,
                eventType,
                LocalDate.now(clock).atStartOfDay(),
                LocalDate.now(clock).plusDays(1).atStartOfDay().minusNanos(1)
        );
    }

    @Transactional(readOnly = true)
    public boolean wasMembershipReminderSentToday(UUID membershipId, NotificationEventType eventType) {
        return notificationLogRepository.existsByMembershipIdAndEventTypeAndCreatedAtBetween(
                membershipId,
                eventType,
                LocalDate.now(clock).atStartOfDay(),
                LocalDate.now(clock).plusDays(1).atStartOfDay().minusNanos(1)
        );
    }

    private void queueMemberCardSnapshot(MemberCardService.MemberCardSnapshot snapshot) {
        Member member = snapshot.member();
        Membership membership = snapshot.membership();
        String subject = notificationProperties.getBrandName() + (membership == null ? " member card" : " membership card");
        String messageBody = notificationMessageFactory.buildMemberCardMessage(member, membership);

        int queued = 0;
        queued += queueMemberEmail(member, null, membership, NotificationEventType.MEMBER_CARD_SHARED, subject, messageBody, snapshot.attachment());
        queued += queueMemberTelegram(member, null, membership, NotificationEventType.MEMBER_CARD_SHARED, subject, messageBody, snapshot.attachment());
        ensureQueued(queued, "No email or Telegram recipient is configured for this member card");
    }

    private int queueMemberWhatsApp(
            Member member,
            Invoice invoice,
            Membership membership,
            NotificationEventType eventType,
            String subject,
            String messageBody
    ) {
        if (!notificationProperties.getWhatsapp().isEnabled() || !hasText(member.getPreferredWhatsappNumber())) {
            return 0;
        }

        return createAndPublishLog(
                member,
                invoice,
                membership,
                eventType,
                NotificationChannel.WHATSAPP,
                member.getPreferredWhatsappNumber().trim(),
                subject,
                messageBody
        );
    }

    private int queueMemberEmail(
            Member member,
            Invoice invoice,
            Membership membership,
            NotificationEventType eventType,
            String subject,
            String messageBody
    ) {
        return queueMemberEmail(member, invoice, membership, eventType, subject, messageBody, null);
    }

    private int queueMemberEmail(
            Member member,
            Invoice invoice,
            Membership membership,
            NotificationEventType eventType,
            String subject,
            String messageBody,
            NotificationAttachment attachment
    ) {
        if (!notificationProperties.getEmail().isEnabled() || !hasText(member.getEmail())) {
            return 0;
        }

        return createAndPublishLog(
                member,
                invoice,
                membership,
                eventType,
                NotificationChannel.EMAIL,
                member.getEmail().trim(),
                subject,
                messageBody,
                attachment
        );
    }

    private int queueMemberTelegram(
            Member member,
            Invoice invoice,
            Membership membership,
            NotificationEventType eventType,
            String subject,
            String messageBody
    ) {
        return queueMemberTelegram(member, invoice, membership, eventType, subject, messageBody, null);
    }

    private int queueMemberTelegram(
            Member member,
            Invoice invoice,
            Membership membership,
            NotificationEventType eventType,
            String subject,
            String messageBody,
            NotificationAttachment attachment
    ) {
        if (!notificationProperties.getTelegram().isEnabled() || !hasText(member.getTelegramChatId())) {
            return 0;
        }

        return createAndPublishLog(
                member,
                invoice,
                membership,
                eventType,
                NotificationChannel.TELEGRAM,
                member.getTelegramChatId().trim(),
                subject,
                messageBody,
                attachment
        );
    }

    private int queueAdminTelegramCopy(
            Member member,
            Invoice invoice,
            Membership membership,
            NotificationEventType eventType,
            String subject,
            String messageBody
    ) {
        return queueAdminTelegramCopy(member, invoice, membership, eventType, subject, messageBody, null);
    }

    private int queueAdminTelegramCopy(
            Member member,
            Invoice invoice,
            Membership membership,
            NotificationEventType eventType,
            String subject,
            String messageBody,
            NotificationAttachment attachment
    ) {
        if (!notificationProperties.getTelegram().isEnabled() || !hasText(notificationProperties.getTelegram().getChatId())) {
            return 0;
        }

        String adminMessage = buildAdminTelegramCopy(member, eventType, messageBody);
        return createAndPublishLog(
                member,
                invoice,
                membership,
                eventType,
                NotificationChannel.TELEGRAM,
                notificationProperties.getTelegram().getChatId().trim(),
                subject,
                adminMessage,
                attachment
        );
    }

    private int createAndPublishLog(
            Member member,
            Invoice invoice,
            Membership membership,
            NotificationEventType eventType,
            NotificationChannel channel,
            String recipient,
            String subject,
            String messageBody
    ) {
        return createAndPublishLog(member, invoice, membership, eventType, channel, recipient, subject, messageBody, null);
    }

    private int createAndPublishLog(
            Member member,
            Invoice invoice,
            Membership membership,
            NotificationEventType eventType,
            NotificationChannel channel,
            String recipient,
            String subject,
            String messageBody,
            NotificationAttachment attachment
    ) {
        NotificationLog log = createPendingLog(member, invoice, membership, eventType, channel, recipient, messageBody);
        notificationPublisher.publish(toMessage(log, channel, subject, attachment));
        return 1;
    }

    private NotificationLog createPendingLog(
            Member member,
            Invoice invoice,
            Membership membership,
            NotificationEventType eventType,
            NotificationChannel channel,
            String recipient,
            String messageBody
    ) {
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setMember(member);
        notificationLog.setInvoice(invoice);
        notificationLog.setMembership(membership);
        notificationLog.setEventType(eventType);
        notificationLog.setChannel(channel.name());
        notificationLog.setRecipient(recipient);
        notificationLog.setMessageBody(messageBody);
        notificationLog.setStatus(NotificationStatus.PENDING);
        return notificationLogRepository.save(notificationLog);
    }

    private NotificationEventMessage toMessage(
            NotificationLog log,
            NotificationChannel channel,
            String subject,
            NotificationAttachment attachment
    ) {
        return new NotificationEventMessage(
                log.getId(),
                log.getMember() == null ? null : log.getMember().getId(),
                log.getEventType(),
                channel,
                log.getRecipient(),
                subject,
                log.getMessageBody(),
                LocalDateTime.now(clock),
                attachment
        );
    }

    private NotificationLog findLog(UUID logId) {
        return notificationLogRepository.findById(logId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification log not found"));
    }

    private void ensureQueued(int queued, String message) {
        if (queued == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String buildAdminTelegramCopy(Member member, NotificationEventType eventType, String messageBody) {
        String memberLabel = member == null ? "System" : member.getFullName();
        return "[" + eventType.name() + "] " + memberLabel + "\n\n" + messageBody;
    }
}