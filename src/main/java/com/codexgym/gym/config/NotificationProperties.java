package com.codexgym.gym.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "gym.notifications")
public class NotificationProperties {

    @PositiveOrZero
    private int feeReminderWindowDays = 5;

    @PositiveOrZero
    private int renewalReminderWindowDays = 7;

    @NotBlank
    private String schedulerCron = "0 0 9 * * *";

    @NotBlank
    private String exchange = "gym.notifications.exchange";

    @NotBlank
    private String routingKey = "notifications.dispatch";

    @NotBlank
    private String queue = "gym.notifications.dispatch";

    @NotBlank
    private String deadLetterExchange = "gym.notifications.dlx";

    @NotBlank
    private String deadLetterQueue = "gym.notifications.dispatch.dlq";

    @NotBlank
    private String deadLetterRoutingKey = "notifications.dispatch.dlq";

    @NotBlank
    private String brandName = "Codex Gym";

    @Valid
    @NotNull
    private WhatsAppProperties whatsapp = new WhatsAppProperties();

    @Valid
    @NotNull
    private EmailProperties email = new EmailProperties();

    @Valid
    @NotNull
    private TelegramProperties telegram = new TelegramProperties();

    @Getter
    @Setter
    public static class WhatsAppProperties {

        private boolean enabled = true;

        private boolean dryRun = true;

        private String webhookUrl;

        private String bearerToken;
    }

    @Getter
    @Setter
    public static class EmailProperties {

        private boolean enabled = true;

        private String from;

        private String adminRecipient;
    }

    @Getter
    @Setter
    public static class TelegramProperties {

        private boolean enabled = false;

        private boolean dryRun = true;

        private String botToken;

        private String botUsername;

        private String chatId;

        private String apiBaseUrl = "https://api.telegram.org";

        @PositiveOrZero
        private long connectTokenTtlMinutes = 30;

        private String deepLinkBaseUrl = "https://t.me";
    }
}
