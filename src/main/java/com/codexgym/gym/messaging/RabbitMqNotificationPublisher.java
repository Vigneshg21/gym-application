package com.codexgym.gym.messaging;

import com.codexgym.gym.config.NotificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqNotificationPublisher implements NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final NotificationProperties notificationProperties;

    @Override
    public void publish(NotificationEventMessage message) {
        rabbitTemplate.convertAndSend(
                notificationProperties.getExchange(),
                notificationProperties.getRoutingKey(),
                message
        );
    }
}

