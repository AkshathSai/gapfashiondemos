package com.gap.ecommerceapp.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
    }

    @Test
    void handleResourceNotFoundException() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource not found", response.getBody().get("message"));
    }

    @Test
    void handleInsufficientStockException() {
        // Given
        InsufficientStockException exception = new InsufficientStockException("Insufficient stock");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleInsufficientStockException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Insufficient stock", response.getBody().get("message"));
    }

    @Test
    void handleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid argument", response.getBody().get("message"));
    }

    @Test
    void handleGenericException() {
        // Given
        Exception exception = new Exception("Generic error");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }
}
