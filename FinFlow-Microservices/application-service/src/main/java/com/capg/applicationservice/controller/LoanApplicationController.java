package com.capg.applicationservice.controller;

import com.capg.applicationservice.dto.ApplicationRequest;
import com.capg.applicationservice.entity.LoanApplication;
import com.capg.applicationservice.service.LoanApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationController.class);
    private final LoanApplicationService service;

    public LoanApplicationController(LoanApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<LoanApplication> createApplication(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ApplicationRequest request) {
        log.info("POST /applications — userId: {}", userId);
        return ResponseEntity.ok(service.createApplication(userId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LoanApplication>> getMyApplications(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /applications/my — userId: {}", userId);
        return ResponseEntity.ok(service.getMyApplications(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanApplication> updateApplication(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ApplicationRequest request) {
        log.info("PUT /applications/{} — userId: {}", id, userId);
        return ResponseEntity.ok(service.updateApplication(id, userId, request));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<String> submitApplication(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /applications/{}/submit — userId: {}", id, userId);
        service.submitApplication(id, userId);
        return ResponseEntity.ok("Application submitted successfully");
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<String> getStatus(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /applications/{}/status — userId: {}", id, userId);
        return ResponseEntity.ok(service.getStatus(id, userId));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<LoanApplication>> getAllApplications() {
        log.info("GET /applications/admin/all");
        return ResponseEntity.ok(service.getAllApplications());
    }
}
