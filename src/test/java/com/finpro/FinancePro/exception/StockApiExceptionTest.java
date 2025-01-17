package com.finpro.FinancePro.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockApiExceptionTest {

    @Test
    void testStockApiException() {
        // Arrange
        String errorMessage = "API Error Message";

        // Act
        StockApiException exception = new StockApiException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testStockApiExceptionInheritance() {
        // Arrange & Act
        StockApiException exception = new StockApiException("Test message");

        // Assert
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testStockApiExceptionStackTrace() {
        // Arrange
        StockApiException exception = new StockApiException("Test message");

        // Act & Assert
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }
}
