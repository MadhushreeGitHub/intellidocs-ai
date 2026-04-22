package com.intellidocs.intellidocs_ai.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String INGESTION_QUEUE    = "document.ingestion";
    public static final String INGESTION_DLQ      = "document.ingestion.dlq";
    public static final String INGESTION_EXCHANGE = "document.exchange";
    public static final String INGESTION_ROUTING_KEY = "document.ingestion";

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(INGESTION_DLQ).build();
    }

    @Bean
    public Queue ingestionQueue() {
        return QueueBuilder.durable(INGESTION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", INGESTION_DLQ)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public DirectExchange ingestionExchange() {
        return new DirectExchange(INGESTION_EXCHANGE);
    }

    @Bean
    public Binding ingestionBinding() {
        // No parameters — reference beans directly to avoid autowiring confusion
        return BindingBuilder
                .bind(ingestionQueue())
                .to(ingestionExchange())
                .with(INGESTION_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}