package com.capg.authservice.service;

import com.capg.authservice.dto.AuthRequest;
import com.capg.authservice.dto.AuthResponse;
import com.capg.authservice.dto.RegisterRequest;
import com.capg.authservice.entity.User;
import com.capg.authservice.repository.UserRepository;
import com.capg.authservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    private User testUser;
    private RegisterRequest registerRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Deepak Kumar");
        testUser.setEmail("deepak@capg.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("ROLE_APPLICANT");

        registerRequest = new RegisterRequest();
        registerRequest.setName("Deepak Kumar");
        registerRequest.setEmail("deepak@capg.com");
        registerRequest.setPassword("Deepak@123");
        registerRequest.setRole("ROLE_APPLICANT");

        authRequest = new AuthRequest();
        authRequest.setEmail("deepak@capg.com");
        authRequest.setPassword("Deepak@123");
    }

    @Test
    @DisplayName("Register — should register user successfully")
    void register_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String result = authService.register(registerRequest);

        assertEquals("User registered successfully", result);
        verify(userRepository).findByEmail("deepak@capg.com");
        verify(passwordEncoder).encode("Deepak@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register — should throw when email already exists")
    void register_DuplicateEmail() {
        when(userRepository.findByEmail("deepak@capg.com")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login — should return token and role on valid credentials")
    void login_Success() {
        when(userRepository.findByEmail("deepak@capg.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Deepak@123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("deepak@capg.com", "ROLE_APPLICANT", 1L)).thenReturn("jwt-token");

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("ROLE_APPLICANT", response.getRole());
    }

    @Test
    @DisplayName("Login — should throw when user not found")
    void login_UserNotFound() {
        when(userRepository.findByEmail("deepak@capg.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(authRequest));
    }

    @Test
    @DisplayName("Login — should throw on invalid password")
    void login_InvalidPassword() {
        when(userRepository.findByEmail("deepak@capg.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Deepak@123", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(authRequest));

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    @DisplayName("GetAllUsers — should return all users")
    void getAllUsers_Success() {
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Admin User");
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<User> users = authService.getAllUsers();

        assertEquals(2, users.size());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("UpdateUser — should update user fields")
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        RegisterRequest updateReq = new RegisterRequest();
        updateReq.setName("Updated Name");
        updateReq.setRole("ROLE_ADMIN");

        String result = authService.updateUser(1L, updateReq);

        assertEquals("User updated successfully", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("UpdateUser — should throw when user not found")
    void updateUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.updateUser(99L, registerRequest));
    }
}
