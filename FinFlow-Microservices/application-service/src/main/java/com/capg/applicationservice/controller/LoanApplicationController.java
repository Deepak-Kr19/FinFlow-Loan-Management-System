package com.capg.applicationservice.controller;

import com.capg.applicationservice.dto.ApplicationRequest;
import com.capg.applicationservice.entity.LoanApplication;
import com.capg.applicationservice.service.LoanApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

    private final LoanApplicationService service;

    public LoanApplicationController(LoanApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<LoanApplication> createApplication(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(service.createApplication(userId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LoanApplication>> getMyApplications(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.getMyApplications(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanApplication> updateApplication(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(service.updateApplication(id, userId, request));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<String> submitApplication(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        service.submitApplication(id, userId);
        return ResponseEntity.ok("Application submitted successfully");
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<String> getStatus(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.getStatus(id, userId));
    }
}
