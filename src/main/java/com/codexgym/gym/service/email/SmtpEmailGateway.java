package com.codexgym.gym.service.email;

import com.codexgym.gym.config.NotificationProperties;
import com.codexgym.gym.messaging.NotificationAttachment;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailGateway implements EmailGateway {

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;
    private final NotificationProperties notificationProperties;

    @Override
    public void sendMessage(String recipient, String subject, String messageBody) {
        if (!notificationProperties.getEmail().isEnabled()) {
            log.info("Email delivery disabled. Skipping message to {}", recipient);
            return;
        }

        String fromAddress = resolveFromAddress();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(messageBody);
        javaMailSender.send(message);
    }

    @Override
    public void sendMessage(String recipient, String subject, String messageBody, NotificationAttachment attachment) {
        if (attachment == null || attachment.getContent() == null || attachment.getContent().length == 0) {
            sendMessage(recipient, subject, messageBody);
            return;
        }
        if (!notificationProperties.getEmail().isEnabled()) {
            log.info("Email delivery disabled. Skipping message to {}", recipient);
            return;
        }

        try {
            var mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(resolveFromAddress());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(messageBody, false);
            helper.addAttachment(
                    attachment.getFileName(),
                    new ByteArrayResource(attachment.getContent()),
                    attachment.getContentType() == null || attachment.getContentType().isBlank()
                            ? "application/octet-stream"
                            : attachment.getContentType()
            );
            javaMailSender.send(mimeMessage);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to send email with attachment", exception);
        }
    }

    private String resolveFromAddress() {
        String fromAddress = firstNonBlank(
                notificationProperties.getEmail().getFrom(),
                mailProperties.getUsername(),
                notificationProperties.getEmail().getAdminRecipient()
        );

        if (fromAddress == null) {
            throw new IllegalStateException("Email sender address is not configured");
        }
        return fromAddress;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}