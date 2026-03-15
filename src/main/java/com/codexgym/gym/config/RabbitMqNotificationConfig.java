package com.codexgym.gym.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NotificationProperties.class)
public class RabbitMqNotificationConfig {

    @Bean
    TopicExchange notificationExchange(NotificationProperties properties) {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    TopicExchange deadLetterExchange(NotificationProperties properties) {
        return new TopicExchange(properties.getDeadLetterExchange(), true, false);
    }

    @Bean
    Queue notificationQueue(NotificationProperties properties) {
        return org.springframework.amqp.core.QueueBuilder.durable(properties.getQueue())
                .withArgument("x-dead-letter-exchange", properties.getDeadLetterExchange())
                .withArgument("x-dead-letter-routing-key", properties.getDeadLetterRoutingKey())
                .build();
    }

    @Bean
    Queue deadLetterQueue(NotificationProperties properties) {
        return org.springframework.amqp.core.QueueBuilder.durable(properties.getDeadLetterQueue()).build();
    }

    @Bean
    Binding notificationBinding(
            Queue notificationQueue,
            TopicExchange notificationExchange,
            NotificationProperties properties
    ) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange)
                .with(properties.getRoutingKey());
    }

    @Bean
    Binding deadLetterBinding(
            Queue deadLetterQueue,
            TopicExchange deadLetterExchange,
            NotificationProperties properties
    ) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(properties.getDeadLetterRoutingKey());
    }

    @Bean
    MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}

