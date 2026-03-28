package com.capg.applicationservice.service;

import com.capg.applicationservice.dto.ApplicationRequest;
import com.capg.applicationservice.entity.LoanApplication;
import com.capg.applicationservice.event.ApplicationEvent;
import com.capg.applicationservice.event.ApplicationEventProducer;
import com.capg.applicationservice.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository repository;

    @Mock
    private ApplicationEventProducer eventProducer;

    @InjectMocks
    private LoanApplicationService service;

    private LoanApplication testApp;
    private ApplicationRequest request;

    @BeforeEach
    void setUp() {
        testApp = new LoanApplication();
        testApp.setId(1L);
        testApp.setUserId(10L);
        testApp.setPersonalDetails("John Doe, 30 years");
        testApp.setEmploymentDetails("Software Engineer, 5 LPA");
        testApp.setLoanDetails("Home Loan, 20 Lakhs");
        testApp.setStatus("Draft");

        request = new ApplicationRequest();
        request.setPersonalDetails("John Doe, 30 years");
        request.setEmploymentDetails("Software Engineer, 5 LPA");
        request.setLoanDetails("Home Loan, 20 Lakhs");
    }

    @Test
    @DisplayName("Create — should create application with Draft status")
    void createApplication_Success() {
        when(repository.save(any(LoanApplication.class))).thenReturn(testApp);

        LoanApplication result = service.createApplication(10L, request);

        assertNotNull(result);
        assertEquals("Draft", result.getStatus());
        assertEquals(10L, result.getUserId());
        verify(repository).save(any(LoanApplication.class));
    }

    @Test
    @DisplayName("GetMyApplications — should return user's applications")
    void getMyApplications_Success() {
        when(repository.findByUserId(10L)).thenReturn(Arrays.asList(testApp));

        List<LoanApplication> results = service.getMyApplications(10L);

        assertEquals(1, results.size());
        assertEquals(10L, results.get(0).getUserId());
    }

    @Test
    @DisplayName("Update — should update application details")
    void updateApplication_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(testApp));
        when(repository.save(any(LoanApplication.class))).thenReturn(testApp);

        ApplicationRequest updateReq = new ApplicationRequest();
        updateReq.setPersonalDetails("Jane Doe, 28 years");
        updateReq.setEmploymentDetails("Senior Dev, 10 LPA");
        updateReq.setLoanDetails("Car Loan, 5 Lakhs");

        LoanApplication result = service.updateApplication(1L, 10L, updateReq);

        assertNotNull(result);
        verify(repository).save(any(LoanApplication.class));
    }

    @Test
    @DisplayName("Update — should throw when unauthorized user")
    void updateApplication_Unauthorized() {
        when(repository.findById(1L)).thenReturn(Optional.of(testApp));

        assertThrows(RuntimeException.class,
                () -> service.updateApplication(1L, 999L, request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Submit — should change status to Submitted and publish event")
    void submitApplication_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(testApp));
        when(repository.save(any(LoanApplication.class))).thenReturn(testApp);

        service.submitApplication(1L, 10L);

        assertEquals("Submitted", testApp.getStatus());
        verify(repository).save(testApp);
        verify(eventProducer).publishApplicationSubmitted(any(ApplicationEvent.class));
    }

    @Test
    @DisplayName("Submit — should throw when application not found")
    void submitApplication_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.submitApplication(99L, 10L));
    }

    @Test
    @DisplayName("GetStatus — should return application status")
    void getStatus_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(testApp));

        String status = service.getStatus(1L, 10L);

        assertEquals("Draft", status);
    }

    @Test
    @DisplayName("GetAll — should return all applications")
    void getAllApplications_Success() {
        LoanApplication app2 = new LoanApplication();
        app2.setId(2L);
        app2.setUserId(20L);
        when(repository.findAll()).thenReturn(Arrays.asList(testApp, app2));

        List<LoanApplication> results = service.getAllApplications();

        assertEquals(2, results.size());
    }
}
