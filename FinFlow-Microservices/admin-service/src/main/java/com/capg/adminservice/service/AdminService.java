package com.capg.adminservice.service;

import com.capg.adminservice.entity.Decision;
import com.capg.adminservice.entity.Report;
import com.capg.adminservice.repository.DecisionRepository;
import com.capg.adminservice.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Service
public class AdminService {

    private final DecisionRepository decisionRepository;
    private final ReportRepository reportRepository;
    private final RestTemplate restTemplate;

    public AdminService(DecisionRepository decisionRepository, ReportRepository reportRepository, RestTemplate restTemplate) {
        this.decisionRepository = decisionRepository;
        this.reportRepository = reportRepository;
        this.restTemplate = restTemplate;
    }

    @Value("${services.application-service}")
    private String applicationServiceUrl;

    @Value("${services.auth-service}")
    private String authServiceUrl;

    public Object getAllApplications() {
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(applicationServiceUrl + "/applications/admin/all", Object.class);
            return response.getBody();
        } catch (Exception e) {
            return "Application Service unavailable or endpoint missing";
        }
    }

    public Decision makeDecision(Long applicationId, String decisionStr, String remarks) {
        Decision decision = new Decision();
        decision.setApplicationId(applicationId);
        decision.setDecision(decisionStr);
        decision.setRemarks(remarks);
        return decisionRepository.save(decision);
    }

    public List<Report> getReports() {
        return reportRepository.findAll();
    }

    public Object getAllUsers() {
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(authServiceUrl + "/auth/admin/users", Object.class);
            return response.getBody();
        } catch (Exception e) {
            return "Auth Service unavailable or endpoint missing";
        }
    }

    public Object updateUser(Long id, Object userUpdate) {
        try {
            restTemplate.put(authServiceUrl + "/auth/admin/users/" + id, userUpdate);
            return "User updated";
        } catch (Exception e) {
            return "Update failed or endpoint missing";
        }
    }
}
