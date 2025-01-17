package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateInvestmentDTO;
import com.finpro.FinancePro.dto.Request.UpdateInvestmentDTO;
import com.finpro.FinancePro.dto.Response.InvestmentResponseDTO;
import com.finpro.FinancePro.dto.Response.StockQuoteDTO;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
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
class InvestmentControllerTest {

    @Autowired
    private InvestmentController investmentController;

    @Autowired
    private UserRepository userRepository;

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
        ResponseEntity<?> response = investmentController.createInvestment(createDTO);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof InvestmentResponseDTO);

        InvestmentResponseDTO investment = (InvestmentResponseDTO) response.getBody();
        assertEquals(createDTO.getType(), investment.getType());
        assertEquals(createDTO.getSymbol(), investment.getSymbol());
        assertEquals(createDTO.getQuantity(), investment.getQuantity());
        assertEquals(createDTO.getPurchasePrice(), investment.getPurchasePrice());
    }

    @Test
    void testUpdateInvestment_Success() {
        // First create an investment
        ResponseEntity<?> createResponse = investmentController.createInvestment(createDTO);
        InvestmentResponseDTO created = (InvestmentResponseDTO) createResponse.getBody();

        // Then update it
        ResponseEntity<InvestmentResponseDTO> updateResponse =
                investmentController.updateInvestment(created.getId(), updateDTO);

        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(updateResponse.getBody());

        InvestmentResponseDTO updated = updateResponse.getBody();
        assertEquals(updateDTO.getType(), updated.getType());
        assertEquals(updateDTO.getQuantity(), updated.getQuantity());
        assertEquals(updateDTO.getPurchasePrice(), updated.getPurchasePrice());
    }

    @Test
    void testGetUserInvestments() {
        // Create two investments
        investmentController.createInvestment(createDTO);
        createDTO.setSymbol("GOOGL");
        createDTO.setDescription("Google stock investment");
        investmentController.createInvestment(createDTO);

        ResponseEntity<List<InvestmentResponseDTO>> response =
                investmentController.getUserInvestments(testUser.getId());

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetInvestment_Success() {
        ResponseEntity<?> createResponse = investmentController.createInvestment(createDTO);
        InvestmentResponseDTO created = (InvestmentResponseDTO) createResponse.getBody();

        ResponseEntity<InvestmentResponseDTO> response =
                investmentController.getInvestment(created.getId());

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(created.getId(), response.getBody().getId());
    }

    @Test
    void testDeleteInvestment_Success() {
        ResponseEntity<?> createResponse = investmentController.createInvestment(createDTO);
        InvestmentResponseDTO created = (InvestmentResponseDTO) createResponse.getBody();

        ResponseEntity<Void> deleteResponse =
                investmentController.deleteInvestment(created.getId());

        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());

        // Verify investment is deleted
        ResponseEntity<List<InvestmentResponseDTO>> investments =
                investmentController.getUserInvestments(testUser.getId());
        assertTrue(investments.getBody().isEmpty());
    }

    @Test
    void testGetPortfolioPerformance() {
        // Create two investments
        investmentController.createInvestment(createDTO);
        createDTO.setSymbol("GOOGL");
        createDTO.setPurchasePrice(2500.0);
        createDTO.setQuantity(2.0);
        investmentController.createInvestment(createDTO);

        ResponseEntity<Map<String, Double>> response =
                investmentController.getPortfolioPerformance(testUser.getId());

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());

        Map<String, Double> performance = response.getBody();
        assertTrue(performance.containsKey("totalInvested"));
        assertTrue(performance.containsKey("currentValue"));
        assertTrue(performance.containsKey("profitLoss"));
        assertTrue(performance.containsKey("returnPercentage"));

        double expectedTotalInvested = (150.0 * 10.0) + (2500.0 * 2.0);
        assertEquals(expectedTotalInvested, performance.get("totalInvested"));
    }

    @Test
    void testGetStockQuote_Success() {
        ResponseEntity<?> response = investmentController.getStockQuote("AAPL");

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof StockQuoteDTO);

        StockQuoteDTO quote = (StockQuoteDTO) response.getBody();
        assertEquals("AAPL", quote.getSymbol());
        assertNotNull(quote.getCurrentPrice());
    }
}