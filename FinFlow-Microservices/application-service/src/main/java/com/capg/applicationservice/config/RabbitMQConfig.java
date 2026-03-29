package com.capg.applicationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the Application Service.
 *
 * Defines the messaging topology:
 * - 1 Topic Exchange: finflow.exchange
 * - 3 Queues: application.submitted, decision.made, document.uploaded
 * - 3 Bindings: routing keys map to queues via the exchange
 *
 * Message flow:
 * Producer → Exchange → (routing key match) → Queue → Consumer
 */
@Configuration
public class RabbitMQConfig {

    // --- Exchange ---
    /** Central topic exchange shared by all FinFlow services */
    public static final String EXCHANGE = "finflow.exchange";

    // --- Queues ---
    /** Queue for application submission events (produced here, consumed by admin-service) */
    public static final String QUEUE_APPLICATION_SUBMITTED = "queue.application.submitted";

    /** Queue for decision events (produced by admin-service, consumed here) */
    public static final String QUEUE_DECISION_MADE = "queue.decision.made";

    /** Queue for document upload events (produced by document-service, consumed here) */
    public static final String QUEUE_DOCUMENT_UPLOADED = "queue.document.uploaded";

    // --- Routing Keys ---
    public static final String KEY_APPLICATION_SUBMITTED = "application.submitted";
    public static final String KEY_DECISION_MADE = "decision.made";
    public static final String KEY_DOCUMENT_UPLOADED = "document.uploaded";

    /** Creates the topic exchange — routes messages based on routing key patterns */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    /** Durable queue for application submitted events (survives broker restart) */
    @Bean
    public Queue applicationSubmittedQueue() {
        return new Queue(QUEUE_APPLICATION_SUBMITTED, true);
    }

    /** Durable queue for decision made events */
    @Bean
    public Queue decisionMadeQueue() {
        return new Queue(QUEUE_DECISION_MADE, true);
    }

    /** Durable queue for document uploaded events */
    @Bean
    public Queue documentUploadedQueue() {
        return new Queue(QUEUE_DOCUMENT_UPLOADED, true);
    }

    /** Binds application.submitted queue to the exchange with its routing key */
    @Bean
    public Binding applicationSubmittedBinding(Queue applicationSubmittedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(applicationSubmittedQueue).to(exchange).with(KEY_APPLICATION_SUBMITTED);
    }

    /** Binds decision.made queue to the exchange with its routing key */
    @Bean
    public Binding decisionMadeBinding(Queue decisionMadeQueue, TopicExchange exchange) {
        return BindingBuilder.bind(decisionMadeQueue).to(exchange).with(KEY_DECISION_MADE);
    }

    /** Binds document.uploaded queue to the exchange with its routing key */
    @Bean
    public Binding documentUploadedBinding(Queue documentUploadedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(documentUploadedQueue).to(exchange).with(KEY_DOCUMENT_UPLOADED);
    }

    /** JSON message converter — serializes/deserializes events as JSON for RabbitMQ */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /** Custom RabbitTemplate with JSON converter for sending messages */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
