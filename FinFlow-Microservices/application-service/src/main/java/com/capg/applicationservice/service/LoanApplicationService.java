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

/**
 * Service layer for loan application business logic.
 * Handles the full lifecycle: create → update → submit → track status.
 * On submission, publishes an APPLICATION_SUBMITTED event to RabbitMQ
 * for the Admin Service to process.
 */
@Service
public class LoanApplicationService {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationService.class);

    private final LoanApplicationRepository repository;
    private final ApplicationEventProducer eventProducer;

    public LoanApplicationService(LoanApplicationRepository repository, ApplicationEventProducer eventProducer) {
        this.repository = repository;
        this.eventProducer = eventProducer;
    }

    /**
     * Creates a new loan application in "Draft" status.
     * The application is not yet submitted for review.
     *
     * @param userId  the ID of the user creating the application (from JWT)
     * @param request the application details (personal, employment, loan info)
     * @return the saved application entity with generated ID
     */
    public LoanApplication createApplication(Long userId, ApplicationRequest request) {
        log.info("Creating new loan application for userId: {}", userId);
        LoanApplication app = new LoanApplication();
        app.setUserId(userId);
        app.setPersonalDetails(request.getPersonalDetails());
        app.setEmploymentDetails(request.getEmploymentDetails());
        app.setLoanDetails(request.getLoanDetails());
        app.setStatus("Draft"); // Initial status
        LoanApplication saved = repository.save(app);
        log.info("Application created: id={}, userId={}, status=Draft", saved.getId(), userId);
        return saved;
    }

    /**
     * Fetches all loan applications belonging to a specific user.
     *
     * @param userId the user's ID (from JWT via X-User-Id header)
     * @return list of the user's loan applications
     */
    public List<LoanApplication> getMyApplications(Long userId) {
        log.info("Fetching applications for userId: {}", userId);
        List<LoanApplication> apps = repository.findByUserId(userId);
        log.debug("Found {} applications for userId: {}", apps.size(), userId);
        return apps;
    }

    /**
     * Updates an existing draft application.
     * Only the application owner can update their own application.
     *
     * @param id      the application ID
     * @param userId  the requesting user's ID (ownership check)
     * @param request updated application details
     * @return the updated application entity
     * @throws RuntimeException if application not found or user is not the owner
     */
    public LoanApplication updateApplication(Long id, Long userId, ApplicationRequest request) {
        log.info("Updating application id={} by userId={}", id, userId);
        LoanApplication app = repository.findById(id)
            .orElseThrow(() -> {
                log.warn("Application not found: id={}", id);
                return new RuntimeException("Application not found");
            });

        // Ownership check: only the creator can update
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

    /**
     * Submits an application for admin review.
     * Changes status from "Draft" to "Submitted" and publishes
     * an APPLICATION_SUBMITTED event to RabbitMQ for the Admin Service.
     *
     * @param id     the application ID to submit
     * @param userId the requesting user's ID (ownership check)
     * @throws RuntimeException if application not found or user is not the owner
     */
    public void submitApplication(Long id, Long userId) {
        log.info("Submitting application id={} by userId={}", id, userId);
        LoanApplication app = repository.findById(id)
            .orElseThrow(() -> {
                log.warn("Submit failed — application not found: id={}", id);
                return new RuntimeException("Application not found");
            });

        // Ownership check
        if (!app.getUserId().equals(userId)) {
            log.warn("Unauthorized submit attempt: userId={} on application owned by userId={}", userId, app.getUserId());
            throw new RuntimeException("Unauthorized");
        }

        // Update status and persist
        app.setStatus("Submitted");
        repository.save(app);
        log.info("Application submitted: id={}, status=Submitted", id);

        // Publish event to RabbitMQ for Admin Service to consume
        ApplicationEvent event = new ApplicationEvent(app.getId(), userId, "Submitted", "APPLICATION_SUBMITTED");
        eventProducer.publishApplicationSubmitted(event);
        log.info("Published APPLICATION_SUBMITTED event for applicationId={}", id);
    }

    /**
     * Gets the current status of a loan application.
     */
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

    /**
     * Fetches a single application by ID with ownership check.
     *
     * @param id     the application ID
     * @param userId the requesting user's ID (ownership check)
     * @return the loan application entity
     */
    public LoanApplication getApplicationById(Long id, Long userId) {
        log.info("Fetching application id={} for userId={}", id, userId);
        LoanApplication app = repository.findById(id)
            .orElseThrow(() -> {
                log.warn("Application not found: id={}", id);
                return new RuntimeException("Application not found");
            });
        if (!app.getUserId().equals(userId)) {
            log.warn("Unauthorized access: userId={} on application owned by userId={}", userId, app.getUserId());
            throw new RuntimeException("Unauthorized");
        }
        return app;
    }

    /**
     * Fetches all applications across all users (admin-only operation).
     * Called by the Admin Service via inter-service REST call.
     *
     * @return list of all loan applications in the system
     */
    public List<LoanApplication> getAllApplications() {
        log.info("Admin fetching all applications");
        List<LoanApplication> apps = repository.findAll();
        log.info("Total applications: {}", apps.size());
        return apps;
    }
}
