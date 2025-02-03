package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateInvestmentDTO;
import com.finpro.FinancePro.dto.Request.CreateUserDTO;
import com.finpro.FinancePro.dto.Request.UpdateInvestmentDTO;
import com.finpro.FinancePro.dto.Response.InvestmentResponseDTO;
import com.finpro.FinancePro.entity.Investment;
import com.finpro.FinancePro.entity.Provider;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.CustomAccessDeniedException;
import com.finpro.FinancePro.repository.InvestmentRepository;
import com.finpro.FinancePro.repository.UserRepository;
import com.finpro.FinancePro.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class InvestmentControllerTest {

    @Autowired
    private InvestmentController investmentController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    public void setUp() {
        // Create a test user with a unique full name
        testUser = new User();
        testUser.setEmail("test" + System.currentTimeMillis() + "@example.com");
        testUser.setFullName("Test User " + System.currentTimeMillis());
        testUser.setPassword("password");
        testUser.setProvider(Provider.SELF);
        testUser = userRepository.save(testUser);

        // Set the test user ID for SecurityUtils
        SecurityUtils.setTestUserId(testUser.getId());
    }

    @Test
    public void testCreateInvestment() {
        CreateInvestmentDTO createDTO = new CreateInvestmentDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setType("Stock");
        createDTO.setSymbol("AAPL");
        createDTO.setQuantity(10.0);
        createDTO.setPurchasePrice(150.0);

        ResponseEntity<?> response = investmentController.createInvestment(createDTO);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody() instanceof InvestmentResponseDTO);

        InvestmentResponseDTO investment = (InvestmentResponseDTO) response.getBody();
        assertEquals("AAPL", investment.getSymbol());
        assertEquals(10.0, investment.getQuantity());
    }

    @Test
    public void testUpdateInvestment() {
        // First create an investment
        CreateInvestmentDTO createDTO = new CreateInvestmentDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setType("Stock");
        createDTO.setSymbol("AAPL");
        createDTO.setQuantity(10.0);
        createDTO.setPurchasePrice(150.0);

        ResponseEntity<?> createResponse = investmentController.createInvestment(createDTO);
        InvestmentResponseDTO createdInvestment = (InvestmentResponseDTO) createResponse.getBody();

        // Prepare update DTO
        UpdateInvestmentDTO updateDTO = new UpdateInvestmentDTO();
        updateDTO.setInvestmentId(createdInvestment.getId());
        updateDTO.setQuantity(15.0);
        updateDTO.setPurchasePrice(160.0);
        updateDTO.setDescription("Updated investment");

        ResponseEntity<InvestmentResponseDTO> updateResponse = investmentController.updateInvestment(testUser.getId(), updateDTO);

        InvestmentResponseDTO updatedInvestment = updateResponse.getBody();
        assertEquals(15.0, updatedInvestment.getQuantity());
        assertEquals(160.0, updatedInvestment.getPurchasePrice());
        assertEquals("Updated investment", updatedInvestment.getDescription());
    }

    @Test
    public void testGetUserInvestments() {
        // Create multiple investments
        CreateInvestmentDTO investment1 = new CreateInvestmentDTO();
        investment1.setUserId(testUser.getId());
        investment1.setType("Stock");
        investment1.setSymbol("AAPL");
        investment1.setQuantity(10.0);
        investment1.setPurchasePrice(150.0);

        CreateInvestmentDTO investment2 = new CreateInvestmentDTO();
        investment2.setUserId(testUser.getId());
        investment2.setType("Bond");
        investment2.setSymbol("BOND");
        investment2.setQuantity(5.0);
        investment2.setPurchasePrice(1000.0);

        investmentController.createInvestment(investment1);
        investmentController.createInvestment(investment2);

        ResponseEntity<List<InvestmentResponseDTO>> response = investmentController.getUserInvestments(testUser.getId());

        List<InvestmentResponseDTO> investments = response.getBody();
        assertNotNull(investments);
        assertEquals(2, investments.size());
    }

    @Test
    public void testGetInvestment() {
        // Create an investment
        CreateInvestmentDTO createDTO = new CreateInvestmentDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setType("Stock");
        createDTO.setSymbol("AAPL");
        createDTO.setQuantity(10.0);
        createDTO.setPurchasePrice(150.0);

        ResponseEntity<?> createResponse = investmentController.createInvestment(createDTO);
        InvestmentResponseDTO createdInvestment = (InvestmentResponseDTO) createResponse.getBody();

        // Retrieve the investment
        ResponseEntity<InvestmentResponseDTO> response = investmentController.getInvestment(createdInvestment.getId());

        InvestmentResponseDTO retrievedInvestment = response.getBody();
        assertNotNull(retrievedInvestment);
        assertEquals(createdInvestment.getId(), retrievedInvestment.getId());
    }

    @Test
    public void testDeleteInvestment() {
        // Create an investment
        CreateInvestmentDTO createDTO = new CreateInvestmentDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setType("Stock");
        createDTO.setSymbol("AAPL");
        createDTO.setQuantity(10.0);
        createDTO.setPurchasePrice(150.0);

        ResponseEntity<?> createResponse = investmentController.createInvestment(createDTO);
        InvestmentResponseDTO createdInvestment = (InvestmentResponseDTO) createResponse.getBody();

        // Delete the investment
        ResponseEntity<Void> deleteResponse = investmentController.deleteInvestment(createdInvestment.getId());

        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());

        // Verify investment is deleted
        assertThrows(Exception.class, () -> {
            investmentController.getInvestment(createdInvestment.getId());
        });
    }

    @Test
    public void testGetPortfolioPerformance() {
        // Create an investment
        CreateInvestmentDTO createDTO = new CreateInvestmentDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setType("Stock");
        createDTO.setSymbol("AAPL");
        createDTO.setQuantity(10.0);
        createDTO.setPurchasePrice(150.0);

        investmentController.createInvestment(createDTO);

        // Get portfolio performance
        ResponseEntity<Map<String, Double>> response = investmentController.getPortfolioPerformance(testUser.getId());

        Map<String, Double> performance = response.getBody();
        assertNotNull(performance);
        assertTrue(performance.containsKey("totalInvested"));
        assertTrue(performance.containsKey("currentValue"));
        assertTrue(performance.containsKey("profitLoss"));
        assertTrue(performance.containsKey("returnPercentage"));
    }

    @Test
    public void testAccessDeniedForUnauthorizedUser() {
        // Create another user
        User anotherUser = new User();
        anotherUser.setEmail("another" + System.currentTimeMillis() + "@example.com");
        anotherUser.setFullName("Another User");
        anotherUser.setPassword(passwordEncoder.encode("password"));
        anotherUser.setProvider(Provider.SELF);
        anotherUser = userRepository.save(anotherUser);

        // Create an investment for the first user
        CreateInvestmentDTO createDTO = new CreateInvestmentDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setType("Stock");
        createDTO.setSymbol("AAPL");
        createDTO.setQuantity(10.0);
        createDTO.setPurchasePrice(150.0);

        ResponseEntity<?> createResponse = investmentController.createInvestment(createDTO);
        InvestmentResponseDTO createdInvestment = (InvestmentResponseDTO) createResponse.getBody();

        // Set security context to another user
        SecurityUtils.setTestUserId(anotherUser.getId());

        // Try to access or modify investment of another user
        assertThrows(CustomAccessDeniedException.class, () -> {
            investmentController.getInvestment(createdInvestment.getId());
        });

        User finalAnotherUser = anotherUser;
        assertThrows(CustomAccessDeniedException.class, () -> {
            UpdateInvestmentDTO updateDTO = new UpdateInvestmentDTO();
            updateDTO.setInvestmentId(createdInvestment.getId());
            updateDTO.setQuantity(15.0);
            investmentController.updateInvestment(finalAnotherUser.getId(), updateDTO);
        });
    }

    @BeforeEach
    public void tearDown() {
        // Clear the test user ID
        SecurityUtils.clearTestUserId();
    }
}