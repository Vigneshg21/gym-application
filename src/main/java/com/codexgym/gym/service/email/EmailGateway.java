package com.codexgym.gym.service.email;

import com.codexgym.gym.messaging.NotificationAttachment;

public interface EmailGateway {

    void sendMessage(String recipient, String subject, String messageBody);

    default void sendMessage(String recipient, String subject, String messageBody, NotificationAttachment attachment) {
        sendMessage(recipient, subject, messageBody);
    }
}