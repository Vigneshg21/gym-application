package com.codexgym.gym.messaging;

public interface NotificationPublisher {

    void publish(NotificationEventMessage message);
}

