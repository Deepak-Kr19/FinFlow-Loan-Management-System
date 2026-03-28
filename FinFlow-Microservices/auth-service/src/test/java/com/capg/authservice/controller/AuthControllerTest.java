package com.capg.authservice.controller;

import com.capg.authservice.dto.AuthRequest;
import com.capg.authservice.dto.AuthResponse;
import com.capg.authservice.dto.RegisterRequest;
import com.capg.authservice.entity.User;
import com.capg.authservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /auth/signup — should register user")
    void signup_Success() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn("User registered successfully");

        RegisterRequest request = new RegisterRequest();
        request.setName("Deepak Kumar");
        request.setEmail("deepak@capg.com");
        request.setPassword("Deepak@123");
        request.setRole("ROLE_APPLICANT");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    @DisplayName("POST /auth/login — should return token")
    void login_Success() throws Exception {
        AuthResponse response = new AuthResponse("jwt-token-123", "ROLE_APPLICANT");
        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        AuthRequest request = new AuthRequest();
        request.setEmail("deepak@capg.com");
        request.setPassword("Deepak@123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.role").value("ROLE_APPLICANT"));
    }

    @Test
    @DisplayName("GET /auth/admin/users — should return all users")
    void getAllUsers_Success() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("Deepak");
        user1.setEmail("deepak@capg.com");
        user1.setRole("ROLE_APPLICANT");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Admin");
        user2.setEmail("admin@capg.com");
        user2.setRole("ROLE_ADMIN");

        when(authService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/auth/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Deepak"))
                .andExpect(jsonPath("$[1].role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("PUT /auth/admin/users/{id} — should update user")
    void updateUser_Success() throws Exception {
        when(authService.updateUser(any(), any(RegisterRequest.class)))
                .thenReturn("User updated successfully");

        RegisterRequest request = new RegisterRequest();
        request.setName("Updated Name");
        request.setRole("ROLE_ADMIN");

        mockMvc.perform(put("/auth/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));
    }
}
