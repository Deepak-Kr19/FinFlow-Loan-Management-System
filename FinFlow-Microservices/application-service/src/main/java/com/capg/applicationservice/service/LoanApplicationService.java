package com.capg.applicationservice.service;

import com.capg.applicationservice.dto.ApplicationRequest;
import com.capg.applicationservice.entity.LoanApplication;
import com.capg.applicationservice.event.ApplicationEvent;
import com.capg.applicationservice.event.ApplicationEventProducer;
import com.capg.applicationservice.repository.LoanApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanApplicationService {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationService.class);

    private final LoanApplicationRepository repository;
    private final ApplicationEventProducer eventProducer;

    public LoanApplicationService(LoanApplicationRepository repository, ApplicationEventProducer eventProducer) {
        this.repository = repository;
        this.eventProducer = eventProducer;
    }

    public LoanApplication createApplication(Long userId, ApplicationRequest request) {
        log.info("Creating new loan application for userId: {}", userId);
        LoanApplication app = new LoanApplication();
        app.setUserId(userId);
        app.setPersonalDetails(request.getPersonalDetails());
        app.setEmploymentDetails(request.getEmploymentDetails());
        app.setLoanDetails(request.getLoanDetails());
        app.setStatus("Draft");
        LoanApplication saved = repository.save(app);
        log.info("Application created: id={}, userId={}, status=Draft", saved.getId(), userId);
        return saved;
    }

    public List<LoanApplication> getMyApplications(Long userId) {
        log.info("Fetching applications for userId: {}", userId);
        List<LoanApplication> apps = repository.findByUserId(userId);
        log.debug("Found {} applications for userId: {}", apps.size(), userId);
        return apps;
    }

    public LoanApplication updateApplication(Long id, Long userId, ApplicationRequest request) {
        log.info("Updating application id={} by userId={}", id, userId);
        LoanApplication app = repository.findById(id)
            .orElseThrow(() -> {
                log.warn("Application not found: id={}", id);
                return new RuntimeException("Application not found");
            });
        if (!app.getUserId().equals(userId)) {
            log.warn("Unauthorized update attempt: userId={} tried to update application owned by userId={}", userId, app.getUserId());
            throw new RuntimeException("Unauthorized");
        }

        app.setPersonalDetails(request.getPersonalDetails());
        app.setEmploymentDetails(request.getEmploymentDetails());
        app.setLoanDetails(request.getLoanDetails());
        log.info("Application updated: id={}", id);
        return repository.save(app);
    }

    public void submitApplication(Long id, Long userId) {
        log.info("Submitting application id={} by userId={}", id, userId);
        LoanApplication app = repository.findById(id)
            .orElseThrow(() -> {
                log.warn("Submit failed — application not found: id={}", id);
                return new RuntimeException("Application not found");
            });
        if (!app.getUserId().equals(userId)) {
            log.warn("Unauthorized submit attempt: userId={} on application owned by userId={}", userId, app.getUserId());
            throw new RuntimeException("Unauthorized");
        }

        app.setStatus("Submitted");
        repository.save(app);
        log.info("Application submitted: id={}, status=Submitted", id);

        // Publish event to RabbitMQ
        ApplicationEvent event = new ApplicationEvent(app.getId(), userId, "Submitted", "APPLICATION_SUBMITTED");
        eventProducer.publishApplicationSubmitted(event);
        log.info("Published APPLICATION_SUBMITTED event for applicationId={}", id);
    }

    public String getStatus(Long id, Long userId) {
        log.debug("Getting status for application id={}, userId={}", id, userId);
        LoanApplication app = repository.findById(id)
            .orElseThrow(() -> {
                log.warn("Status check failed — application not found: id={}", id);
                return new RuntimeException("Application not found");
            });
        if (!app.getUserId().equals(userId)) throw new RuntimeException("Unauthorized");
        return app.getStatus();
    }

    public List<LoanApplication> getAllApplications() {
        log.info("Admin fetching all applications");
        List<LoanApplication> apps = repository.findAll();
        log.info("Total applications: {}", apps.size());
        return apps;
    }
}
