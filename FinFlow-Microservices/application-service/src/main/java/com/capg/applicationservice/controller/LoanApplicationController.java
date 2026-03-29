package com.capg.applicationservice.controller;

import com.capg.applicationservice.dto.ApplicationRequest;
import com.capg.applicationservice.entity.LoanApplication;
import com.capg.applicationservice.service.LoanApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for loan application endpoints.
 * Base path: /applications
 *
 * Endpoints:
 * - POST /applications             → Create new application (Draft)
 * - GET  /applications/my          → List current user's applications
 * - PUT  /applications/{id}        → Update a draft application
 * - POST /applications/{id}/submit → Submit application for review
 * - GET  /applications/{id}/status → Check application status
 * - GET  /applications/admin/all   → List all applications (admin only)
 *
 * Note: X-User-Id header is injected by the API Gateway from the JWT token.
 */
@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationController.class);
    private final LoanApplicationService service;

    public LoanApplicationController(LoanApplicationService service) {
        this.service = service;
    }

    /** Create a new loan application in Draft status */
    @PostMapping
    public ResponseEntity<LoanApplication> createApplication(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ApplicationRequest request) {
        log.info("POST /applications — userId: {}", userId);
        return ResponseEntity.ok(service.createApplication(userId, request));
    }

    /** Get all applications belonging to the authenticated user */
    @GetMapping("/my")
    public ResponseEntity<List<LoanApplication>> getMyApplications(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /applications/my — userId: {}", userId);
        return ResponseEntity.ok(service.getMyApplications(userId));
    }

    /** Update an existing draft application */
    @PutMapping("/{id}")
    public ResponseEntity<LoanApplication> updateApplication(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ApplicationRequest request) {
        log.info("PUT /applications/{} — userId: {}", id, userId);
        return ResponseEntity.ok(service.updateApplication(id, userId, request));
    }

    /** Submit an application for admin review (triggers RabbitMQ event) */
    @PostMapping("/{id}/submit")
    public ResponseEntity<String> submitApplication(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /applications/{}/submit — userId: {}", id, userId);
        service.submitApplication(id, userId);
        return ResponseEntity.ok("Application submitted successfully");
    }

    /** Check the current status of an application */
    @GetMapping("/{id}/status")
    public ResponseEntity<String> getStatus(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /applications/{}/status — userId: {}", id, userId);
        return ResponseEntity.ok(service.getStatus(id, userId));
    }

    /** Admin endpoint: fetch all applications across all users */
    @GetMapping("/admin/all")
    public ResponseEntity<List<LoanApplication>> getAllApplications() {
        log.info("GET /applications/admin/all");
        return ResponseEntity.ok(service.getAllApplications());
    }
}
