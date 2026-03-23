package com.finflow.application.service;

import com.finflow.application.dto.*;
import com.finflow.application.entity.*;
import com.finflow.application.repository.ApplicationRepository;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository repo;

    public ApplicationService(ApplicationRepository repo) {
        this.repo = repo;
    }

    public LoanApplication createApplication(ApplicationRequest req) {
        LoanApplication app = new LoanApplication();
        app.setUserId(req.getUserId());
        app.setAmount(req.getAmount());
        app.setPurpose(req.getPurpose());
        app.setStatus(ApplicationStatus.DRAFT);
        app.setCreatedAt(LocalDateTime.now());

        return repo.save(app);
    }

    public List<LoanApplication> getMyApplications(Long userId) {
        return repo.findByUserId(userId);
    }

    public LoanApplication submitApplication(Long id) {
        LoanApplication app = repo.findById(id).orElseThrow();
        app.setStatus(ApplicationStatus.SUBMITTED);
        return repo.save(app);
    }
}