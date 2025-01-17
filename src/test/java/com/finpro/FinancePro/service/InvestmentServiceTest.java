package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateInvestmentDTO;
import com.finpro.FinancePro.dto.Request.UpdateInvestmentDTO;
import com.finpro.FinancePro.dto.Response.InvestmentResponseDTO;
import com.finpro.FinancePro.dto.Response.StockQuoteDTO;
import com.finpro.FinancePro.entity.Investment;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.exception.StockApiException;
import com.finpro.FinancePro.repository.InvestmentRepository;
import com.finpro.FinancePro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "stock.quote.use.mock=true",
        "alphavantage.cache.duration=300000"
})
public class InvestmentServiceTest {

    @Autowired
    private InvestmentService investmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvestmentRepository investmentRepository;

    private User testUser;
    private CreateInvestmentDTO createDTO;
    private UpdateInvestmentDTO updateDTO;

    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = new User();
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);

        // Setup create DTO
        createDTO = new CreateInvestmentDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setType("Stock");
        createDTO.setSymbol("AAPL");
        createDTO.setQuantity(10.0);
        createDTO.setPurchasePrice(150.0);
        createDTO.setDescription("Apple Inc. stock investment");

        // Setup update DTO
        updateDTO = new UpdateInvestmentDTO();
        updateDTO.setType("Stock");
        updateDTO.setQuantity(15.0);
        updateDTO.setPurchasePrice(155.0);
        updateDTO.setDescription("Updated Apple stock investment");
    }

    @Test
    void testCreateInvestment_Success() {
        // Act
        InvestmentResponseDTO response = investmentService.createInvestment(createDTO);

        // Assert
        assertNotNull(response);
        assertEquals(createDTO.getType(), response.getType());
        assertEquals(createDTO.getSymbol(), response.getSymbol());
        assertEquals(createDTO.getQuantity(), response.getQuantity());
        assertEquals(createDTO.getPurchasePrice(), response.getPurchasePrice());
        assertEquals(createDTO.getQuantity() * createDTO.getPurchasePrice(), response.getAmount());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    void testCreateInvestment_InvalidUser() {
        // Arrange
        createDTO.setUserId(999L); // Non-existent user ID

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                investmentService.createInvestment(createDTO)
        );
    }

    @Test
    void testUpdateInvestment_Success() {
        // Arrange
        InvestmentResponseDTO created = investmentService.createInvestment(createDTO);

        // Act
        InvestmentResponseDTO updated = investmentService.updateInvestment(created.getId(), updateDTO);

        // Assert
        assertNotNull(updated);
        assertEquals(updateDTO.getType(), updated.getType());
        assertEquals(updateDTO.getQuantity(), updated.getQuantity());
        assertEquals(updateDTO.getPurchasePrice(), updated.getPurchasePrice());
        assertEquals(updateDTO.getDescription(), updated.getDescription());
        assertEquals(updateDTO.getQuantity() * updateDTO.getPurchasePrice(), updated.getAmount());
    }

    @Test
    void testUpdateInvestment_NotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                investmentService.updateInvestment(999L, updateDTO)
        );
    }

    @Test
    void testGetUserInvestments() {
        // Arrange
        investmentService.createInvestment(createDTO);

        // Create second investment
        createDTO.setSymbol("GOOGL");
        createDTO.setDescription("Google stock investment");
        investmentService.createInvestment(createDTO);

        // Act
        List<InvestmentResponseDTO> investments = investmentService.getUserInvestments(testUser.getId());

        // Assert
        assertNotNull(investments);
        assertEquals(2, investments.size());
    }

    @Test
    void testGetInvestment_Success() {
        // Arrange
        InvestmentResponseDTO created = investmentService.createInvestment(createDTO);

        // Act
        InvestmentResponseDTO retrieved = investmentService.getInvestment(created.getId());

        // Assert
        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals(created.getType(), retrieved.getType());
        assertEquals(created.getSymbol(), retrieved.getSymbol());
    }

    @Test
    void testGetInvestment_NotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                investmentService.getInvestment(999L)
        );
    }

    @Test
    void testDeleteInvestment_Success() {
        // Arrange
        InvestmentResponseDTO created = investmentService.createInvestment(createDTO);

        // Act
        assertDoesNotThrow(() -> investmentService.deleteInvestment(created.getId()));

        // Assert
        assertThrows(ResourceNotFoundException.class, () ->
                investmentService.getInvestment(created.getId())
        );
    }

    @Test
    void testDeleteInvestment_NotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                investmentService.deleteInvestment(999L)
        );
    }

    @Test
    void testGetStockQuote_Success() {
        // Act
        StockQuoteDTO quote = investmentService.getStockQuote("AAPL");

        // Assert
        assertNotNull(quote);
        assertEquals("AAPL", quote.getSymbol());
        assertNotNull(quote.getCurrentPrice());
        assertNotNull(quote.getChange());
        assertNotNull(quote.getChangePercent());
        assertNotNull(quote.getHigh());
        assertNotNull(quote.getLow());
        assertNotNull(quote.getVolume());
    }

    @Test
    void testGetStockQuote_UsesCache() {
        // First call to populate the cache
        StockQuoteDTO firstQuote = investmentService.getStockQuote("AAPL");
        double expectedPrice = firstQuote.getCurrentPrice(); // Instead of hardcoding 150.0

        // Second call should use cache
        StockQuoteDTO cachedQuote = investmentService.getStockQuote("AAPL");

        // Assert
        assertNotNull(cachedQuote);
        assertEquals("AAPL", cachedQuote.getSymbol());
        assertEquals(expectedPrice, cachedQuote.getCurrentPrice());
    }

    @Test
    void testGetStockQuote_UsesMockData() {
        // Arrange
        ReflectionTestUtils.setField(investmentService, "useMockData", true);

        // Act
        StockQuoteDTO mockQuote = investmentService.getStockQuote("AAPL");

        // Assert
        assertNotNull(mockQuote);
        assertEquals("AAPL", mockQuote.getSymbol());
        assertNotNull(mockQuote.getCurrentPrice());
    }


    @Test
    void testCalculatePortfolioPerformance() {
        // Arrange
        investmentService.createInvestment(createDTO);

        // Create second investment
        createDTO.setSymbol("GOOGL");
        createDTO.setPurchasePrice(2500.0);
        createDTO.setQuantity(2.0);
        investmentService.createInvestment(createDTO);

        // Act
        Map<String, Double> performance = investmentService.calculatePortfolioPerformance(testUser.getId());

        // Assert
        assertNotNull(performance);
        assertTrue(performance.containsKey("totalInvested"));
        assertTrue(performance.containsKey("currentValue"));
        assertTrue(performance.containsKey("profitLoss"));
        assertTrue(performance.containsKey("returnPercentage"));

        double expectedTotalInvested = (150.0 * 10.0) + (2500.0 * 2.0); // First investment + Second investment
        assertEquals(expectedTotalInvested, performance.get("totalInvested"));
    }
}