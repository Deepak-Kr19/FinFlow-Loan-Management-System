package com.capg.applicationservice.event;

import com.capg.applicationservice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ApplicationEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventProducer.class);
    private final RabbitTemplate rabbitTemplate;

    public ApplicationEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishApplicationSubmitted(ApplicationEvent event) {
        log.info("📤 Publishing APPLICATION_SUBMITTED event: {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.KEY_APPLICATION_SUBMITTED, event);
    }
}
