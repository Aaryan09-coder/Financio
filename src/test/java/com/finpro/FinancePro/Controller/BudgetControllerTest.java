package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateBudgetDTO;
import com.finpro.FinancePro.dto.Request.UpdateBudgetDTO;
import com.finpro.FinancePro.dto.Response.BudgetResponseDTO;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BudgetControllerTest {

    @Autowired
    private BudgetController budgetController;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private CreateBudgetDTO createDTO;
    private UpdateBudgetDTO updateDTO;

    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = new User();
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);

        // Setup create DTO
        createDTO = new CreateBudgetDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setPeriod("Monthly");
        createDTO.setTotalAmount(1000.0);

        // Setup update DTO
        updateDTO = new UpdateBudgetDTO();
        updateDTO.setTotalAmount(1500.0);
    }

    @Test
    void testCreateBudget_Success() {
        // Act
        ResponseEntity<BudgetResponseDTO> response = budgetController.createBudget(createDTO);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(createDTO.getTotalAmount(), response.getBody().getTotalAmount());
        assertEquals(createDTO.getPeriod(), response.getBody().getPeriod());
        assertEquals(0.0, response.getBody().getSpentAmount());
        assertEquals(createDTO.getTotalAmount(), response.getBody().getRemainingBudget());
    }

    @Test
    void testUpdateBudget_Success() {
        // Arrange
        ResponseEntity<BudgetResponseDTO> created = budgetController.createBudget(createDTO);
        updateDTO.setId(created.getBody().getId());

        // Act
        ResponseEntity<BudgetResponseDTO> response = budgetController.updateBudget(updateDTO);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(updateDTO.getTotalAmount(), response.getBody().getTotalAmount());
        assertEquals(created.getBody().getPeriod(), response.getBody().getPeriod());
    }

    @Test
    void testGetBudgetByUser_Success() {
        // Arrange
        budgetController.createBudget(createDTO);

        // Act
        ResponseEntity<BudgetResponseDTO> response = budgetController.getBudgetByUser(testUser.getId());

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(createDTO.getTotalAmount(), response.getBody().getTotalAmount());
        assertEquals(createDTO.getPeriod(), response.getBody().getPeriod());
    }

    @Test
    void testGetBudgetByUserAndPeriod_Success() {
        // Arrange
        budgetController.createBudget(createDTO);

        // Act
        ResponseEntity<BudgetResponseDTO> response =
                budgetController.getBudgetByUserAndPeriod(testUser.getId(), "Monthly");

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(createDTO.getTotalAmount(), response.getBody().getTotalAmount());
        assertEquals(createDTO.getPeriod(), response.getBody().getPeriod());
    }
}