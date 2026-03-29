package com.capg.adminservice.service;

import com.capg.adminservice.entity.Decision;
import com.capg.adminservice.entity.Report;
import com.capg.adminservice.event.DecisionEvent;
import com.capg.adminservice.event.DecisionEventProducer;
import com.capg.adminservice.repository.DecisionRepository;
import com.capg.adminservice.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final DecisionRepository decisionRepository;
    private final ReportRepository reportRepository;
    private final RestTemplate restTemplate;
    private final DecisionEventProducer eventProducer;

    public AdminService(DecisionRepository decisionRepository, ReportRepository reportRepository,
                        RestTemplate restTemplate, DecisionEventProducer eventProducer) {
        this.decisionRepository = decisionRepository;
        this.reportRepository = reportRepository;
        this.restTemplate = restTemplate;
        this.eventProducer = eventProducer;
    }

    @Value("${services.application-service}")
    private String applicationServiceUrl;

    @Value("${services.auth-service}")
    private String authServiceUrl;

    public Object getAllApplications() {
        log.info("Fetching all applications from application-service: {}", applicationServiceUrl);
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(applicationServiceUrl + "/applications/admin/all", Object.class);
            log.info("Successfully fetched applications from application-service");
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch applications from application-service: {}", e.getMessage());
            return "Application Service unavailable or endpoint missing";
        }
    }

    public Decision makeDecision(Long applicationId, String decisionStr, String remarks) {
        log.info("Making decision on applicationId={}: decision={}, remarks={}", applicationId, decisionStr, remarks);
        Decision decision = new Decision();
        decision.setApplicationId(applicationId);
        decision.setDecision(decisionStr);
        decision.setRemarks(remarks);
        Decision saved = decisionRepository.save(decision);
        log.info("Decision saved: id={}, applicationId={}, decision={}", saved.getId(), applicationId, decisionStr);

        // Publish event to RabbitMQ
        DecisionEvent event = new DecisionEvent(applicationId, decisionStr, remarks);
        eventProducer.publishDecisionMade(event);
        log.info("Published DECISION_MADE event for applicationId={}", applicationId);

        return saved;
    }

    public List<Report> getReports() {
        log.info("Fetching all reports");
        List<Report> reports = reportRepository.findAll();
        log.info("Found {} reports", reports.size());
        return reports;
    }

    public Object getAllUsers() {
        log.info("Fetching all users from auth-service: {}", authServiceUrl);
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(authServiceUrl + "/auth/admin/users", Object.class);
            log.info("Successfully fetched users from auth-service");
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch users from auth-service: {}", e.getMessage());
            return "Auth Service unavailable or endpoint missing";
        }
    }

    public Object updateUser(Long id, Object userUpdate) {
        log.info("Updating user id={} via auth-service", id);
        try {
            restTemplate.put(authServiceUrl + "/auth/admin/users/" + id, userUpdate);
            log.info("User updated successfully via auth-service: id={}", id);
            return "User updated";
        } catch (Exception e) {
            log.error("Failed to update user id={} via auth-service: {}", id, e.getMessage());
            return "Update failed or endpoint missing";
        }
    }
}
