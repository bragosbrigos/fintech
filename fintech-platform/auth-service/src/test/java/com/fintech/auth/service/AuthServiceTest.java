package com.fintech.auth.service;

import com.fintech.auth.entity.User;
import com.fintech.auth.repository.UserRepository;
import com.fintech.platform.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
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

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.register(email, rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            authService.register(email, "password123");
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldAuthenticateUserSuccessfully() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // Act
        User result = authService.authenticate(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertTrue(result.isEnabled());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            authService.authenticate(email, "password123");
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsInvalid() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongpassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            authService.authenticate(email, password);
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserIsDisabled() {
        // Arrange
        testUser.setEnabled(false);
        String email = "test@example.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            authService.authenticate(email, password);
        });

        assertEquals("User account is disabled", exception.getMessage());
    }
}
