package com.capg.adminservice.controller;

import com.capg.adminservice.entity.Decision;
import com.capg.adminservice.entity.Report;
import com.capg.adminservice.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for admin panel endpoints.
 * Base path: /admin
 *
 * Endpoints:
 * - GET  /admin/applications             → List all loan applications (via Application Service)
 * - POST /admin/applications/{id}/decision → Approve or reject an application
 * - GET  /admin/reports                   → List all system reports
 * - GET  /admin/users                     → List all users (via Auth Service)
 * - PUT  /admin/users/{id}               → Update a user (via Auth Service)
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    /** Fetch all loan applications from Application Service */
    @GetMapping("/applications")
    public ResponseEntity<Object> getApplications() {
        log.info("GET /admin/applications");
        return ResponseEntity.ok(service.getAllApplications());
    }

    /** Make a decision (APPROVED/REJECTED) on a loan application */
    @PostMapping("/applications/{id}/decision")
    public ResponseEntity<Decision> makeDecision(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam String remarks) {
        log.info("POST /admin/applications/{}/decision — decision: {}, remarks: {}", id, decision, remarks);
        return ResponseEntity.ok(service.makeDecision(id, decision, remarks));
    }

    /** Fetch all system reports */
    @GetMapping("/reports")
    public ResponseEntity<List<Report>> getReports() {
        log.info("GET /admin/reports");
        return ResponseEntity.ok(service.getReports());
    }

    /** Fetch all registered users from Auth Service */
    @GetMapping("/users")
    public ResponseEntity<Object> getUsers() {
        log.info("GET /admin/users");
        return ResponseEntity.ok(service.getAllUsers());
    }

    /** Update user details via Auth Service */
    @PutMapping("/users/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody Object userUpdate) {
        log.info("PUT /admin/users/{}", id);
        return ResponseEntity.ok(service.updateUser(id, userUpdate));
    }
}
