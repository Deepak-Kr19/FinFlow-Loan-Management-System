package com.capg.adminservice.controller;

import com.capg.adminservice.entity.Decision;
import com.capg.adminservice.entity.Report;
import com.capg.adminservice.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/applications")
    public ResponseEntity<Object> getApplications() {
        log.info("GET /admin/applications");
        return ResponseEntity.ok(service.getAllApplications());
    }

    @PostMapping("/applications/{id}/decision")
    public ResponseEntity<Decision> makeDecision(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam String remarks) {
        log.info("POST /admin/applications/{}/decision — decision: {}, remarks: {}", id, decision, remarks);
        return ResponseEntity.ok(service.makeDecision(id, decision, remarks));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Report>> getReports() {
        log.info("GET /admin/reports");
        return ResponseEntity.ok(service.getReports());
    }

    @GetMapping("/users")
    public ResponseEntity<Object> getUsers() {
        log.info("GET /admin/users");
        return ResponseEntity.ok(service.getAllUsers());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody Object userUpdate) {
        log.info("PUT /admin/users/{}", id);
        return ResponseEntity.ok(service.updateUser(id, userUpdate));
    }
}
