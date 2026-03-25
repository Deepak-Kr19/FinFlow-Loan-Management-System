package com.capg.adminservice.controller;

import com.capg.adminservice.entity.Decision;
import com.capg.adminservice.entity.Report;
import com.capg.adminservice.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/applications")
    public ResponseEntity<Object> getApplications() {
        return ResponseEntity.ok(service.getAllApplications());
    }

    @PostMapping("/applications/{id}/decision")
    public ResponseEntity<Decision> makeDecision(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam String remarks) {
        return ResponseEntity.ok(service.makeDecision(id, decision, remarks));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Report>> getReports() {
        return ResponseEntity.ok(service.getReports());
    }

    @GetMapping("/users")
    public ResponseEntity<Object> getUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody Object userUpdate) {
        return ResponseEntity.ok(service.updateUser(id, userUpdate));
    }
}
