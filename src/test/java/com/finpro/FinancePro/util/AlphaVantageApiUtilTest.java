package com.finpro.FinancePro.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class AlphaVantageApiUtilTest {

    private AlphaVantageApiUtil apiUtil;
    private static final String TEST_API_KEY = "testApiKey123";
    private static final String TEST_BASE_URL = "https://www.alphavantage.co/query";

    @BeforeEach
    void setUp() {
        apiUtil = new AlphaVantageApiUtil();
        ReflectionTestUtils.setField(apiUtil, "apiKey", TEST_API_KEY);
        ReflectionTestUtils.setField(apiUtil, "baseUrl", TEST_BASE_URL);
    }

    @Test
    void testBuildStockQuoteUrl_Success() {
        String symbol = "AAPL";
        String url = apiUtil.buildStockQuoteUrl(symbol);

        assertTrue(url.contains(TEST_BASE_URL));
        assertTrue(url.contains("function=TIME_SERIES_INTRADAY"));
        assertTrue(url.contains("symbol=" + symbol));
        assertTrue(url.contains("interval=5min"));
        assertTrue(url.contains("apikey=" + TEST_API_KEY));
    }

    @Test
    void testBuildStockQuoteUrl_WithSpecialCharacters() {
        String symbol = "BRK.A";
        String url = apiUtil.buildStockQuoteUrl(symbol);

        assertTrue(url.contains("symbol=" + symbol));
        assertFalse(url.contains(" "));
    }

    @Test
    void testBuildStockQuoteUrl_ConsistentFormat() {
        String symbol1 = "AAPL";
        String symbol2 = "MSFT";

        String url1 = apiUtil.buildStockQuoteUrl(symbol1);
        String url2 = apiUtil.buildStockQuoteUrl(symbol2);

        String url1WithoutSymbol = url1.replace(symbol1, "SYMBOL");
        String url2WithoutSymbol = url2.replace(symbol2, "SYMBOL");
        assertEquals(url1WithoutSymbol, url2WithoutSymbol);
    }
}
