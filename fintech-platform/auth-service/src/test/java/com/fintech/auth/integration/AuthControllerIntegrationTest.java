package com.fintech.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RedisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:15")
    )
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Container
    static RedisContainer redis = new RedisContainer(
        DockerImageName.parse("redis:7-alpine")
    );

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Limpar banco de dados antes de cada teste se necessário
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("email", "integration@test.com");
        request.put("password", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.email", is("integration@test.com")))
            .andExpect(jsonPath("$.data.enabled", is(true)));
    }

    @Test
    void shouldLoginSuccessfullyAfterRegistration() throws Exception {
        // Arrange - First register a user
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "login@test.com");
        registerRequest.put("password", "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated());

        // Act - Then login
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "login@test.com");
        loginRequest.put("password", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.accessToken", notNullValue()))
            .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
            .andExpect(jsonPath("$.data.tokenType", is("Bearer")));
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("email", "invalid-email");
        request.put("password", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictForDuplicateEmail() throws Exception {
        // Arrange - Register first user
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "duplicate@test.com");
        registerRequest.put("password", "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated());

        // Act - Try to register again with same email
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("email", "nonexistent@test.com");
        request.put("password", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
