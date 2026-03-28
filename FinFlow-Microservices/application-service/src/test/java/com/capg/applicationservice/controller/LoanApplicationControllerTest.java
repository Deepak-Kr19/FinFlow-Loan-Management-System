package com.capg.applicationservice.controller;

import com.capg.applicationservice.dto.ApplicationRequest;
import com.capg.applicationservice.entity.LoanApplication;
import com.capg.applicationservice.service.LoanApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanApplicationController.class)
class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanApplicationService service;

    @Autowired
    private ObjectMapper objectMapper;

    private LoanApplication testApp;

    @BeforeEach
    void setUp() {
        testApp = new LoanApplication();
        testApp.setId(1L);
        testApp.setUserId(10L);
        testApp.setPersonalDetails("John Doe");
        testApp.setEmploymentDetails("Engineer");
        testApp.setLoanDetails("Home Loan 20L");
        testApp.setStatus("Draft");
    }

    @Test
    @DisplayName("POST /applications — should create application")
    void createApplication() throws Exception {
        when(service.createApplication(eq(10L), any(ApplicationRequest.class))).thenReturn(testApp);

        ApplicationRequest request = new ApplicationRequest();
        request.setPersonalDetails("John Doe");
        request.setEmploymentDetails("Engineer");
        request.setLoanDetails("Home Loan 20L");

        mockMvc.perform(post("/applications")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Draft"))
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    @DisplayName("GET /applications/my — should return user applications")
    void getMyApplications() throws Exception {
        when(service.getMyApplications(10L)).thenReturn(Arrays.asList(testApp));

        mockMvc.perform(get("/applications/my")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("POST /applications/{id}/submit — should submit application")
    void submitApplication() throws Exception {
        mockMvc.perform(post("/applications/1/submit")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Application submitted successfully"));
    }

    @Test
    @DisplayName("GET /applications/{id}/status — should return status")
    void getStatus() throws Exception {
        when(service.getStatus(1L, 10L)).thenReturn("Submitted");

        mockMvc.perform(get("/applications/1/status")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Submitted"));
    }

    @Test
    @DisplayName("GET /applications/admin/all — should return all applications")
    void getAllApplications() throws Exception {
        when(service.getAllApplications()).thenReturn(Arrays.asList(testApp));

        mockMvc.perform(get("/applications/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
