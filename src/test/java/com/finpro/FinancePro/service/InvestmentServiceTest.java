package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateInvestmentDTO;
import com.finpro.FinancePro.dto.Request.UpdateInvestmentDTO;
import com.finpro.FinancePro.dto.Response.InvestmentResponseDTO;
import com.finpro.FinancePro.entity.Investment;
import com.finpro.FinancePro.entity.Provider;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.InvestmentRepository;
import com.finpro.FinancePro.repository.UserRepository;
import com.finpro.FinancePro.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class InvestmentServiceTest {

    @Autowired
    private InvestmentService investmentService;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private CreateInvestmentDTO createInvestmentDTO;

    @BeforeEach
    public void setup() {
        // Create a test user with a unique full name
        testUser = new User();
        testUser.setEmail("test" + System.currentTimeMillis() + "@example.com");
        testUser.setFullName("Test User " + System.currentTimeMillis());
        testUser.setPassword("password");
        testUser.setProvider(Provider.SELF);
        testUser = userRepository.save(testUser);

        // Set the test user ID for SecurityUtils
        SecurityUtils.setTestUserId(testUser.getId());

        // Prepare a sample investment DTO
        createInvestmentDTO = new CreateInvestmentDTO();
        createInvestmentDTO.setUserId(testUser.getId());
        createInvestmentDTO.setType("Stock");
        createInvestmentDTO.setSymbol("AAPL");
        createInvestmentDTO.setQuantity(10.0);
        createInvestmentDTO.setPurchasePrice(150.0);
    }

    @Test
    public void testCreateInvestment() {
        // Create investment
        InvestmentResponseDTO createdInvestment = investmentService.createInvestment(createInvestmentDTO);

        // Assertions
        assertNotNull(createdInvestment);
        assertEquals(createInvestmentDTO.getType(), createdInvestment.getType());
        assertEquals(createInvestmentDTO.getSymbol(), createdInvestment.getSymbol());
        assertEquals(createInvestmentDTO.getQuantity(), createdInvestment.getQuantity());
        assertEquals(createInvestmentDTO.getPurchasePrice(), createdInvestment.getPurchasePrice());
        assertEquals(testUser.getId(), createdInvestment.getUserId());
    }

    @Test
    public void testUpdateInvestment() {
        // First create an investment
        InvestmentResponseDTO createdInvestment = investmentService.createInvestment(createInvestmentDTO);

        // Prepare update DTO
        UpdateInvestmentDTO updateDTO = new UpdateInvestmentDTO();
        updateDTO.setInvestmentId(createdInvestment.getId());
        updateDTO.setQuantity(15.0);
        updateDTO.setPurchasePrice(160.0);
        updateDTO.setDescription("Updated investment");

        // Update investment
        InvestmentResponseDTO updatedInvestment = investmentService.updateInvestment(testUser.getId(), updateDTO);

        // Assertions
        assertNotNull(updatedInvestment);
        assertEquals(15.0, updatedInvestment.getQuantity());
        assertEquals(160.0, updatedInvestment.getPurchasePrice());
        assertEquals("Updated investment", updatedInvestment.getDescription());
    }

    @Test
    public void testGetUserInvestments() {
        // Create multiple investments
        investmentService.createInvestment(createInvestmentDTO);

        CreateInvestmentDTO anotherInvestmentDTO = new CreateInvestmentDTO();
        anotherInvestmentDTO.setUserId(testUser.getId());
        anotherInvestmentDTO.setType("Bond");
        anotherInvestmentDTO.setSymbol("BOND");
        anotherInvestmentDTO.setQuantity(5.0);
        anotherInvestmentDTO.setPurchasePrice(1000.0);
        investmentService.createInvestment(anotherInvestmentDTO);

        // Get user investments
        List<InvestmentResponseDTO> userInvestments = investmentService.getUserInvestments(testUser.getId());

        // Assertions
        assertNotNull(userInvestments);
        assertEquals(2, userInvestments.size());
    }

    @Test
    public void testGetInvestment() {
        // Create investment
        InvestmentResponseDTO createdInvestment = investmentService.createInvestment(createInvestmentDTO);

        // Retrieve the investment
        InvestmentResponseDTO retrievedInvestment = investmentService.getInvestment(createdInvestment.getId());

        // Assertions
        assertNotNull(retrievedInvestment);
        assertEquals(createdInvestment.getId(), retrievedInvestment.getId());
    }

    @Test
    public void testDeleteInvestment() {
        // Create investment
        InvestmentResponseDTO createdInvestment = investmentService.createInvestment(createInvestmentDTO);

        // Delete the investment
        investmentService.deleteInvestment(createdInvestment.getId());

        // Try to retrieve the deleted investment (should throw exception)
        assertThrows(ResourceNotFoundException.class, () -> {
            investmentService.getInvestment(createdInvestment.getId());
        });
    }

    @Test
    public void testCalculatePortfolioPerformance() {
        // Create investments
        investmentService.createInvestment(createInvestmentDTO);

        // Calculate portfolio performance
        Map<String, Double> performance = investmentService.calculatePortfolioPerformance(testUser.getId());

        // Assertions
        assertNotNull(performance);
        assertTrue(performance.containsKey("totalInvested"));
        assertTrue(performance.containsKey("currentValue"));
        assertTrue(performance.containsKey("profitLoss"));
        assertTrue(performance.containsKey("returnPercentage"));
    }

    @BeforeEach
    public void tearDown() {
        // Clear the test user ID
        SecurityUtils.clearTestUserId();
    }
}