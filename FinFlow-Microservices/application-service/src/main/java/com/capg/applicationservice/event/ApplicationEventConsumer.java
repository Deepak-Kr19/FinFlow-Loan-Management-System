package com.capg.applicationservice.event;

import com.capg.applicationservice.config.RabbitMQConfig;
import com.capg.applicationservice.entity.LoanApplication;
import com.capg.applicationservice.repository.LoanApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ApplicationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventConsumer.class);
    private final LoanApplicationRepository repository;

    public ApplicationEventConsumer(LoanApplicationRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DECISION_MADE)
    public void handleDecisionMade(Map<String, Object> event) {
        log.info("📥 Received DECISION_MADE event: {}", event);
        try {
            Long applicationId = Long.valueOf(event.get("applicationId").toString());
            String decision = event.get("decision").toString();
            Optional<LoanApplication> appOpt = repository.findById(applicationId);
            if (appOpt.isPresent()) {
                LoanApplication app = appOpt.get();
                app.setStatus(decision);
                repository.save(app);
                log.info("✅ Application {} status updated to: {}", applicationId, decision);
            }
        } catch (Exception e) {
            log.error("❌ Error processing DECISION_MADE event", e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DOCUMENT_UPLOADED)
    public void handleDocumentUploaded(Map<String, Object> event) {
        log.info("📥 Received DOCUMENT_UPLOADED event: {}", event);
        log.info("📎 Document uploaded for application {}: type={}, status={}",
                event.get("applicationId"), event.get("type"), event.get("status"));
    }
}
