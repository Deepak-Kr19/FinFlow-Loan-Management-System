package com.capg.adminservice.service;

import com.capg.adminservice.entity.Decision;
import com.capg.adminservice.entity.Report;
import com.capg.adminservice.event.DecisionEvent;
import com.capg.adminservice.event.DecisionEventProducer;
import com.capg.adminservice.repository.DecisionRepository;
import com.capg.adminservice.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DecisionEventProducer eventProducer;

    @InjectMocks
    private AdminService adminService;

    private Decision testDecision;

    @BeforeEach
    void setUp() {
        testDecision = new Decision();
        testDecision.setId(1L);
        testDecision.setApplicationId(100L);
        testDecision.setDecision("APPROVED");
        testDecision.setRemarks("All documents verified");
    }

    @Test
    @DisplayName("MakeDecision — should save decision and publish RabbitMQ event")
    void makeDecision_Approve() {
        when(decisionRepository.save(any(Decision.class))).thenReturn(testDecision);

        Decision result = adminService.makeDecision(100L, "APPROVED", "All documents verified");

        assertNotNull(result);
        assertEquals("APPROVED", result.getDecision());
        assertEquals(100L, result.getApplicationId());
        verify(decisionRepository).save(any(Decision.class));
        verify(eventProducer).publishDecisionMade(any(DecisionEvent.class));
    }

    @Test
    @DisplayName("MakeDecision — should handle REJECTED decision")
    void makeDecision_Reject() {
        Decision rejected = new Decision();
        rejected.setId(2L);
        rejected.setApplicationId(100L);
        rejected.setDecision("REJECTED");
        rejected.setRemarks("Insufficient income");

        when(decisionRepository.save(any(Decision.class))).thenReturn(rejected);

        Decision result = adminService.makeDecision(100L, "REJECTED", "Insufficient income");

        assertEquals("REJECTED", result.getDecision());
        verify(eventProducer).publishDecisionMade(any(DecisionEvent.class));
    }

    @Test
    @DisplayName("GetReports — should return all reports")
    void getReports_Success() {
        Report report1 = new Report();
        report1.setId(1L);
        report1.setType("MONTHLY");
        Report report2 = new Report();
        report2.setId(2L);
        report2.setType("ANNUAL");

        when(reportRepository.findAll()).thenReturn(Arrays.asList(report1, report2));

        List<Report> reports = adminService.getReports();

        assertEquals(2, reports.size());
        verify(reportRepository).findAll();
    }

    @Test
    @DisplayName("GetAllApplications — should handle service unavailable gracefully")
    void getAllApplications_ServiceDown() {
        org.springframework.test.util.ReflectionTestUtils.setField(
                adminService, "applicationServiceUrl", "http://localhost:8082");

        when(restTemplate.getForEntity(any(String.class), eq(Object.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        Object result = adminService.getAllApplications();

        assertEquals("Application Service unavailable or endpoint missing", result);
    }

    @Test
    @DisplayName("GetAllUsers — should handle service unavailable gracefully")
    void getAllUsers_ServiceDown() {
        org.springframework.test.util.ReflectionTestUtils.setField(
                adminService, "authServiceUrl", "http://localhost:8081");

        when(restTemplate.getForEntity(any(String.class), eq(Object.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        Object result = adminService.getAllUsers();

        assertEquals("Auth Service unavailable or endpoint missing", result);
    }
}
