package com.capg.applicationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "finflow.exchange";
    public static final String QUEUE_APPLICATION_SUBMITTED = "queue.application.submitted";
    public static final String QUEUE_DECISION_MADE = "queue.decision.made";
    public static final String QUEUE_DOCUMENT_UPLOADED = "queue.document.uploaded";
    public static final String KEY_APPLICATION_SUBMITTED = "application.submitted";
    public static final String KEY_DECISION_MADE = "decision.made";
    public static final String KEY_DOCUMENT_UPLOADED = "document.uploaded";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue applicationSubmittedQueue() {
        return new Queue(QUEUE_APPLICATION_SUBMITTED, true);
    }

    @Bean
    public Queue decisionMadeQueue() {
        return new Queue(QUEUE_DECISION_MADE, true);
    }

    @Bean
    public Queue documentUploadedQueue() {
        return new Queue(QUEUE_DOCUMENT_UPLOADED, true);
    }

    @Bean
    public Binding applicationSubmittedBinding(Queue applicationSubmittedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(applicationSubmittedQueue).to(exchange).with(KEY_APPLICATION_SUBMITTED);
    }

    @Bean
    public Binding decisionMadeBinding(Queue decisionMadeQueue, TopicExchange exchange) {
        return BindingBuilder.bind(decisionMadeQueue).to(exchange).with(KEY_DECISION_MADE);
    }

    @Bean
    public Binding documentUploadedBinding(Queue documentUploadedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(documentUploadedQueue).to(exchange).with(KEY_DOCUMENT_UPLOADED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
