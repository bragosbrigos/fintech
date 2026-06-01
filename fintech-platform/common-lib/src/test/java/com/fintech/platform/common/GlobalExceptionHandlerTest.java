package com.fintech.platform.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void testApiExceptionReturnsCorrectResponse() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ApiException exception = new ApiException("Test error", "TEST_ERROR");

        // Act
        ErrorResponse response = handler.handleApiException(exception);

        // Assert
        assertNotNull(response);
        assertEquals("TEST_ERROR", response.getCode());
        assertEquals("Test error", response.getMessage());
    }

    @Test
    void testIllegalArgumentExceptionReturnsCorrectResponse() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // Act
        ErrorResponse response = handler.handleIllegalArgumentException(exception);

        // Assert
        assertNotNull(response);
        assertEquals("BAD_REQUEST", response.getCode());
        assertEquals("Invalid argument", response.getMessage());
    }

    @Test
    void testGenericExceptionReturnsCorrectResponse() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RuntimeException exception = new RuntimeException("Unexpected error");

        // Act
        ErrorResponse response = handler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals("INTERNAL_SERVER_ERROR", response.getCode());
        assertTrue(response.getMessage().contains("Unexpected error"));
    }
}
