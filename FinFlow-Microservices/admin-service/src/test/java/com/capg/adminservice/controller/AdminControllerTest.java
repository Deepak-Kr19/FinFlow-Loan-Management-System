package com.capg.adminservice.controller;

import com.capg.adminservice.entity.Decision;
import com.capg.adminservice.entity.Report;
import com.capg.adminservice.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /admin/applications — should return applications")
    void getApplications() throws Exception {
        Map<String, Object> app = new HashMap<>();
        app.put("id", 1);
        app.put("status", "Submitted");
        when(service.getAllApplications()).thenReturn(Arrays.asList(app));

        mockMvc.perform(get("/admin/applications"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/applications/{id}/decision — should approve application")
    void makeDecision_Approve() throws Exception {
        Decision decision = new Decision();
        decision.setId(1L);
        decision.setApplicationId(100L);
        decision.setDecision("APPROVED");
        decision.setRemarks("All verified");

        when(service.makeDecision(eq(100L), eq("APPROVED"), eq("All verified")))
                .thenReturn(decision);

        mockMvc.perform(post("/admin/applications/100/decision")
                        .param("decision", "APPROVED")
                        .param("remarks", "All verified"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("APPROVED"))
                .andExpect(jsonPath("$.applicationId").value(100));
    }

    @Test
    @DisplayName("POST /admin/applications/{id}/decision — should reject application")
    void makeDecision_Reject() throws Exception {
        Decision decision = new Decision();
        decision.setId(2L);
        decision.setApplicationId(100L);
        decision.setDecision("REJECTED");
        decision.setRemarks("Bad credit score");

        when(service.makeDecision(eq(100L), eq("REJECTED"), eq("Bad credit score")))
                .thenReturn(decision);

        mockMvc.perform(post("/admin/applications/100/decision")
                        .param("decision", "REJECTED")
                        .param("remarks", "Bad credit score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("REJECTED"));
    }

    @Test
    @DisplayName("GET /admin/reports — should return reports")
    void getReports() throws Exception {
        Report report = new Report();
        report.setId(1L);
        report.setType("MONTHLY");
        report.setData("Monthly report data");

        when(service.getReports()).thenReturn(Arrays.asList(report));

        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("MONTHLY"));
    }

    @Test
    @DisplayName("GET /admin/users — should return users")
    void getUsers() throws Exception {
        when(service.getAllUsers()).thenReturn(Arrays.asList("user1", "user2"));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/users/{id} — should update user")
    void updateUser() throws Exception {
        when(service.updateUser(eq(1L), any())).thenReturn("User updated");

        Map<String, String> body = new HashMap<>();
        body.put("name", "Updated Name");

        mockMvc.perform(put("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }
}
