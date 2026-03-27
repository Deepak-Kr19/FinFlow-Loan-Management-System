package com.capg.documentservice.event;

import com.capg.documentservice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(DocumentEventProducer.class);
    private final RabbitTemplate rabbitTemplate;

    public DocumentEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDocumentUploaded(DocumentEvent event) {
        log.info("📤 Publishing DOCUMENT_UPLOADED event: {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.KEY_DOCUMENT_UPLOADED, event);
    }
}
