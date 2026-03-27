package com.capg.adminservice.event;

import com.capg.adminservice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class DecisionEventProducer {

    private static final Logger log = LoggerFactory.getLogger(DecisionEventProducer.class);
    private final RabbitTemplate rabbitTemplate;

    public DecisionEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDecisionMade(DecisionEvent event) {
        log.info("📤 Publishing DECISION_MADE event: {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.KEY_DECISION_MADE, event);
    }
}
