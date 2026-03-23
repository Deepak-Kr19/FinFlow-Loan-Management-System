package com.finflow.application.controller;

import com.finflow.application.dto.*;
import com.finflow.application.entity.LoanApplication;
import com.finflow.application.service.ApplicationService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public LoanApplication create(@RequestBody ApplicationRequest req) {
        return service.createApplication(req);
    }

    @GetMapping("/my/{userId}")
    public List<LoanApplication> getMyApps(@PathVariable Long userId) {
        return service.getMyApplications(userId);
    }

    @PostMapping("/{id}/submit")
    public LoanApplication submit(@PathVariable Long id) {
        return service.submitApplication(id);
    }
}