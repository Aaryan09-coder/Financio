package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateInvestmentDTO;
import com.finpro.FinancePro.dto.Request.UpdateInvestmentDTO;
import com.finpro.FinancePro.dto.Response.InvestmentResponseDTO;
import com.finpro.FinancePro.dto.Response.StockQuoteDTO;
import com.finpro.FinancePro.entity.Investment;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.CustomAccessDeniedException;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.exception.StockApiException;
import com.finpro.FinancePro.repository.InvestmentRepository;
import com.finpro.FinancePro.repository.UserRepository;
import com.finpro.FinancePro.util.AlphaVantageApiUtil;
import com.finpro.FinancePro.util.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class InvestmentService {

    private static final Logger logger = LoggerFactory.getLogger(InvestmentService.class);

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${alphavantage.api.key}")
    private String apiKey;

    private static final String ALPHA_VANTAGE_URL = "https://www.alphavantage.co/query";

    @Autowired
    private AlphaVantageApiUtil alphaVantageApiUtil;

    @Value("${stock.quote.use.mock:false}")
    private boolean useMockData;

    @Value("${alphavantage.cache.duration:300000}")
    private long cacheDuration;  // 5 minutes by default

    @Autowired
    private RateLimiter rateLimiter;

    public InvestmentResponseDTO createInvestment(CreateInvestmentDTO createDTO) {
        User user = userRepository.findById(createDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + createDTO.getUserId()));

        Investment investment = new Investment();
        investment.setUser(user);
        investment.setType(createDTO.getType());
        investment.setDescription(createDTO.getDescription());
        investment.setSymbol(createDTO.getSymbol());
        investment.setQuantity(createDTO.getQuantity());
        investment.setPurchasePrice(createDTO.getPurchasePrice());
        investment.setAmount(createDTO.getQuantity() * createDTO.getPurchasePrice()); // Calculate amount here

        Investment savedInvestment = investmentRepository.save(investment);
        return convertToResponseDTO(savedInvestment);
    }

    @Transactional
    public InvestmentResponseDTO updateInvestment(Long userId, UpdateInvestmentDTO updateDTO){
        Investment investment = investmentRepository.findById(updateDTO.getInvestmentId())
                .orElseThrow(()-> new ResourceNotFoundException("Investment not found with ID: " + updateDTO.getInvestmentId()));

        // Verify that the investment belongs to the specified user
        if (!investment.getUser().getId().equals(userId)) {
            throw new CustomAccessDeniedException("Investment does not belong to the specified user");
        }


        // Update the investment fields if they are present in the DTO
        if (updateDTO.getType() != null) {
            investment.setType(updateDTO.getType());
        }

        if (updateDTO.getDescription() != null) {
            investment.setDescription(updateDTO.getDescription());
        }

        if (updateDTO.getQuantity() != null) {
            investment.setQuantity(updateDTO.getQuantity());
            // Recalculate amount when quantity changes
            investment.setAmount(investment.getQuantity() * investment.getPurchasePrice());
        }
        if (updateDTO.getPurchasePrice() != null) {
            investment.setPurchasePrice(updateDTO.getPurchasePrice());
            // Recalculate amount when purchase price changes
            investment.setAmount(investment.getQuantity() * investment.getPurchasePrice());
        }

        Investment updatedInvestment = investmentRepository.save(investment);
        return convertToResponseDTO(updatedInvestment);
    }

    public List<InvestmentResponseDTO> getUserInvestments(Long userId) {
        List<Investment> investments = investmentRepository.findByUserId(userId);
        return investments.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public InvestmentResponseDTO getInvestment(Long id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found with ID: " + id));
        return convertToResponseDTO(investment);
    }

    public void deleteInvestment(Long id) {
        if (!investmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Investment not found with ID: " + id);
        }
        investmentRepository.deleteById(id);
    }
    //From here the new Cache and get Stock method are started
    // Cache for stock quotes
    private final Map<String, CachedStockQuote> quoteCache = new ConcurrentHashMap<>();

    private static class CachedStockQuote {
        final StockQuoteDTO quote;
        final long timestamp;

        CachedStockQuote(StockQuoteDTO quote) {
            this.quote = quote;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired(long duration) {
            return System.currentTimeMillis() - timestamp > duration;
        }
    }

    public StockQuoteDTO getStockQuote(String symbol) {
        // Return mock data if configured
        if (useMockData) {
            return getMockStockQuote(symbol);
        }

        // Check cache
        CachedStockQuote cached = quoteCache.get(symbol);
        if (cached != null && !cached.isExpired(cacheDuration)) {
            logger.debug("Returning cached quote for symbol: {}", symbol);
            return cached.quote;
        }

        // Check rate limit
        if (!rateLimiter.shouldAllowRequest(symbol)) {
            logger.warn("Rate limit exceeded for symbol: {}. Using cached data if available.", symbol);
            if (cached != null) {
                return cached.quote;
            }
            throw new StockApiException("Rate limit exceeded and no cached data available");
        }

        try {
            String url = alphaVantageApiUtil.buildStockQuoteUrl(symbol);
            logger.debug("Fetching stock quote for symbol: {} from URL: {}", symbol, url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            // Validate response
            if (body == null) {
                throw new StockApiException("Empty response from Alpha Vantage");
            }

            if (body.containsKey("Error Message")) {
                throw new ResourceNotFoundException("Stock data not found for symbol: " + symbol);
            }

            if (body.containsKey("Note")) {
                logger.warn("API rate limit warning: {}", body.get("Note"));
            }

            // Parse time series data
            Map<String, Object> timeSeries = (Map<String, Object>) body.get("Time Series (5min)");
            if (timeSeries == null || timeSeries.isEmpty()) {
                throw new ResourceNotFoundException("No recent data available for symbol: " + symbol);
            }

            // Get most recent data point
            String latestTimestamp = timeSeries.keySet().iterator().next();
            Map<String, String> latestData = (Map<String, String>) timeSeries.get(latestTimestamp);

            // Create and populate StockQuoteDTO
            StockQuoteDTO quote = new StockQuoteDTO();
            quote.setSymbol(symbol);

            try {
                double closePrice = Double.parseDouble(latestData.get("4. close"));
                double openPrice = Double.parseDouble(latestData.get("1. open"));

                quote.setCurrentPrice(closePrice);
                quote.setHigh(Double.parseDouble(latestData.get("2. high")));
                quote.setLow(Double.parseDouble(latestData.get("3. low")));
                quote.setVolume(Long.parseLong(latestData.get("5. volume")));

                // Calculate change and percentage
                double change = closePrice - openPrice;
                double changePercent = (change / openPrice) * 100;

                quote.setChange(roundToTwoDecimals(change));
                quote.setChangePercent(roundToTwoDecimals(changePercent));

            } catch (NumberFormatException e) {
                logger.error("Error parsing numeric values for symbol {}: {}", symbol, e.getMessage());
                throw new StockApiException("Error parsing stock data values");
            }

            // Cache the quote
            quoteCache.put(symbol, new CachedStockQuote(quote));
            return quote;

        } catch (Exception e) {
            logger.error("Error fetching stock quote for {}: {}", symbol, e.getMessage());
            // Try to return expired cache if available during error
            if (cached != null) {
                logger.warn("Returning expired cached data due to API error");
                return cached.quote;
            }
            throw new StockApiException("Failed to fetch stock quote: " + e.getMessage());
        }
    }

    private StockQuoteDTO getMockStockQuote(String symbol) {
        StockQuoteDTO quote = new StockQuoteDTO();
        quote.setSymbol(symbol);
        quote.setCurrentPrice(100.00);
        quote.setChange(2.50);
        quote.setChangePercent(2.50);
        quote.setHigh(102.00);
        quote.setLow(98.00);
        quote.setVolume(1000000L);
        return quote;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }


    public Map<String, Double> calculatePortfolioPerformance(Long userId) {
        List<Investment> investments = investmentRepository.findByUserId(userId);

        // Group by symbol to reduce API calls
        Map<String, Double> totalQuantityBySymbol = investments.stream()
                .collect(Collectors.groupingBy(
                        Investment::getSymbol,
                        Collectors.summingDouble(Investment::getQuantity)
                ));

        // Fetch quotes for unique symbols
        Map<String, StockQuoteDTO> quotes = new HashMap<>();
        for (String symbol : totalQuantityBySymbol.keySet()) {
            try {
                quotes.put(symbol, getStockQuote(symbol));
            } catch (StockApiException e) {
                logger.error("Error fetching quote for {}: {}", symbol, e.getMessage());
                // Continue with other symbols
            }
        }

        double totalInvested = investments.stream()
                .mapToDouble(inv -> inv.getQuantity() * inv.getPurchasePrice())
                .sum();

        double currentValue = investments.stream()
                .filter(inv -> quotes.containsKey(inv.getSymbol()))
                .mapToDouble(inv -> inv.getQuantity() *
                        quotes.get(inv.getSymbol()).getCurrentPrice())
                .sum();

        double profitLoss = currentValue - totalInvested;
        double returnPercentage = (profitLoss / totalInvested) * 100;


        Map<String, Double> performance = new HashMap<>();
        performance.put("totalInvested", totalInvested);
        performance.put("currentValue", currentValue);
        performance.put("profitLoss", profitLoss);
        performance.put("returnPercentage", returnPercentage);

        return performance;
    }

    private InvestmentResponseDTO convertToResponseDTO(Investment investment) {
        InvestmentResponseDTO dto = new InvestmentResponseDTO();
        dto.setId(investment.getId());
        dto.setUserId(investment.getUser().getId());
        dto.setType(investment.getType());
        dto.setAmount(investment.getAmount());
        dto.setDescription(investment.getDescription());
        dto.setSymbol(investment.getSymbol());
        dto.setQuantity(investment.getQuantity());
        dto.setPurchasePrice(investment.getPurchasePrice());
        dto.setCreatedAt(investment.getCreatedAt());
        dto.setUpdatedAt(investment.getUpdatedAt());
        return dto;
    }


}
