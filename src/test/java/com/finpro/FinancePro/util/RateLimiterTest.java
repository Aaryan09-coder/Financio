package com.finpro.FinancePro.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
    }

    @Test
    void testShouldAllowRequest_FirstRequest() {
        String symbol = "AAPL";
        boolean allowed = rateLimiter.shouldAllowRequest(symbol);
        assertTrue(allowed, "First request should always be allowed");
    }

    @Test
    void testShouldAllowRequest_TooFrequent() {
        String symbol = "AAPL";

        boolean firstRequest = rateLimiter.shouldAllowRequest(symbol);
        boolean secondRequest = rateLimiter.shouldAllowRequest(symbol);

        assertTrue(firstRequest, "First request should be allowed");
        assertFalse(secondRequest, "Second immediate request should be denied");
    }

    @Test
    void testShouldAllowRequest_DifferentSymbols() {
        String symbol1 = "AAPL";
        String symbol2 = "GOOGL";

        boolean firstSymbolRequest = rateLimiter.shouldAllowRequest(symbol1);
        boolean secondSymbolRequest = rateLimiter.shouldAllowRequest(symbol2);

        assertTrue(firstSymbolRequest, "Request for first symbol should be allowed");
        assertTrue(secondSymbolRequest, "Request for different symbol should be allowed");
    }

    @Test
    void testShouldAllowRequest_AfterInterval() throws InterruptedException {
        String symbol = "AAPL";

        boolean firstRequest = rateLimiter.shouldAllowRequest(symbol);
        Thread.sleep(13000); // Wait for more than MIN_INTERVAL (12 seconds)
        boolean secondRequest = rateLimiter.shouldAllowRequest(symbol);

        assertTrue(firstRequest, "First request should be allowed");
        assertTrue(secondRequest, "Request after interval should be allowed");
    }

    @Test
    void testShouldAllowRequest_MultipleSymbolsOverTime() throws InterruptedException {
        String symbol1 = "AAPL";
        String symbol2 = "GOOGL";

        assertTrue(rateLimiter.shouldAllowRequest(symbol1), "First request for symbol1 should be allowed");
        assertTrue(rateLimiter.shouldAllowRequest(symbol2), "First request for symbol2 should be allowed");

        assertFalse(rateLimiter.shouldAllowRequest(symbol1), "Immediate second request for symbol1 should be denied");
        assertFalse(rateLimiter.shouldAllowRequest(symbol2), "Immediate second request for symbol2 should be denied");

        Thread.sleep(13000);

        assertTrue(rateLimiter.shouldAllowRequest(symbol1), "Request after interval for symbol1 should be allowed");
        assertTrue(rateLimiter.shouldAllowRequest(symbol2), "Request after interval for symbol2 should be allowed");
    }
}
