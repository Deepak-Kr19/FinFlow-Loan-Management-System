package com.capg.authservice.service;

import com.capg.authservice.dto.AuthRequest;
import com.capg.authservice.dto.AuthResponse;
import com.capg.authservice.dto.RegisterRequest;
import com.capg.authservice.entity.User;
import com.capg.authservice.repository.UserRepository;
import com.capg.authservice.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setName("Test User");
        request.setPassword("password");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded_pw");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        String result = authService.register(request);

        assertEquals("User registered successfully", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        AuthRequest request = new AuthRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("encoded_pw");
        user.setRole("ROLE_APPLICANT");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded_pw")).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com", "ROLE_APPLICANT", 1L)).thenReturn("mock_token");

        AuthResponse response = authService.login(request);

        assertEquals("mock_token", response.getToken());
        assertEquals("ROLE_APPLICANT", response.getRole());
    }
}
