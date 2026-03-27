package com.capg.adminservice.event;

import com.capg.adminservice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ApplicationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_APPLICATION_SUBMITTED)
    public void handleApplicationSubmitted(Map<String, Object> event) {
        log.info("📥 Received APPLICATION_SUBMITTED event: {}", event);
        log.info("🔔 New loan application submitted — AppId: {}, UserId: {}, Status: {}",
                event.get("applicationId"), event.get("userId"), event.get("status"));
    }
}
