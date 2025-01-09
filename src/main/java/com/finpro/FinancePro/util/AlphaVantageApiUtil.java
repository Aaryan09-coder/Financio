package com.finpro.FinancePro.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AlphaVantageApiUtil {

    @Value("${alphavantage.api.key}")
    private String apiKey;

    @Value("${alphavantage.api.base-url:https://www.alphavantage.co/query}")
    private String baseUrl;

    public String buildStockQuoteUrl(String symbol) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("function", "TIME_SERIES_INTRADAY")
                .queryParam("symbol", symbol)
                .queryParam("interval", "5min")
                .queryParam("apikey", apiKey)
                .build()
                .toUriString();
    }
}
