package com.capg.applicationservice.service;

import com.capg.applicationservice.dto.ApplicationRequest;
import com.capg.applicationservice.entity.LoanApplication;
import com.capg.applicationservice.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository repository;

    public LoanApplicationService(LoanApplicationRepository repository) {
        this.repository = repository;
    }

    public LoanApplication createApplication(Long userId, ApplicationRequest request) {
        LoanApplication app = new LoanApplication();
        app.setUserId(userId);
        app.setPersonalDetails(request.getPersonalDetails());
        app.setEmploymentDetails(request.getEmploymentDetails());
        app.setLoanDetails(request.getLoanDetails());
        app.setStatus("Draft");
        return repository.save(app);
    }

    public List<LoanApplication> getMyApplications(Long userId) {
        return repository.findByUserId(userId);
    }

    public LoanApplication updateApplication(Long id, Long userId, ApplicationRequest request) {
        LoanApplication app = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        if (!app.getUserId().equals(userId)) throw new RuntimeException("Unauthorized");

        app.setPersonalDetails(request.getPersonalDetails());
        app.setEmploymentDetails(request.getEmploymentDetails());
        app.setLoanDetails(request.getLoanDetails());
        return repository.save(app);
    }

    public void submitApplication(Long id, Long userId) {
        LoanApplication app = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        if (!app.getUserId().equals(userId)) throw new RuntimeException("Unauthorized");

        app.setStatus("Submitted");
        repository.save(app);
    }

    public String getStatus(Long id, Long userId) {
        LoanApplication app = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        if (!app.getUserId().equals(userId)) throw new RuntimeException("Unauthorized");
        return app.getStatus();
    }
}
