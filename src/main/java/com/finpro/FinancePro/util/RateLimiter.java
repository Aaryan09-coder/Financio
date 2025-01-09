package com.finpro.FinancePro.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {
    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();
    private static final long MIN_INTERVAL = 12000; // 12 seconds between requests

    public boolean shouldAllowRequest(String symbol){
        long currentTime = System.currentTimeMillis();
        Long lastRequest = lastRequestTime.get(symbol);

        if(lastRequest == null || currentTime - lastRequest >= MIN_INTERVAL){
            lastRequestTime.put(symbol, currentTime);
            return true;
        }
        return false;
    }
}
