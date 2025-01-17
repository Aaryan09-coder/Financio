package com.finpro.FinancePro.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GlobalExceptionHandlerTest {

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void testHandleResourceNotFoundException() {
        // Arrange
        String errorMessage = "Resource not found";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleResourceNotFoundException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(errorMessage, responseBody.get("message"));
        assertEquals(HttpStatus.NOT_FOUND.value(), responseBody.get("status"));
        assertTrue(responseBody.get("timestamp") instanceof LocalDateTime);
    }

    @Test
    void testHandleGenericException() {
        // Arrange
        String errorMessage = "Unexpected error";
        Exception ex = new Exception(errorMessage);

        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("An unexpected error occurred: " + errorMessage, responseBody.get("message"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseBody.get("status"));
        assertTrue(responseBody.get("timestamp") instanceof LocalDateTime);
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        // Arrange
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("objectName", "field1", "Field 1 error"));
        fieldErrors.add(new FieldError("objectName", "field2", "Field 2 error"));

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objectName");
        fieldErrors.forEach(bindingResult::addError);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseBody.get("status"));
        assertTrue(responseBody.get("timestamp") instanceof LocalDateTime);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) responseBody.get("errors");
        assertNotNull(errors);
        assertEquals("Field 1 error", errors.get("field1"));
        assertEquals("Field 2 error", errors.get("field2"));
    }

    @Test
    void testHandleInvalidRequestException() {
        // Arrange
        String errorMessage = "Invalid request";
        InvalidRequestException ex = new InvalidRequestException(errorMessage);

        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleInvalidRequestException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(errorMessage, responseBody.get("message"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseBody.get("status"));
        assertTrue(responseBody.get("timestamp") instanceof LocalDateTime);
    }
}
