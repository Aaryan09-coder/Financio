package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateGoalDTO;
import com.finpro.FinancePro.dto.Request.UpdateGoalDTO;
import com.finpro.FinancePro.dto.Response.GoalProgressDTO;
import com.finpro.FinancePro.dto.Response.GoalResponseDTO;
import com.finpro.FinancePro.entity.Budget;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.exception.InvalidRequestException;
import com.finpro.FinancePro.repository.BudgetRepository;
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
public class GoalControllerTest {

    @Autowired
    private GoalController goalController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private User testUser;
    private CreateGoalDTO createDTO;
    private UpdateGoalDTO updateDTO;

    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = new User();
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);

        // Create and save test budget (required for goal calculations)
        Budget testBudget = new Budget();
        testBudget.setUser(testUser);
        testBudget.setTotalAmount(1000.0);
        testBudget.setSpentAmount(0.0);
        testBudget.setPeriod("Monthly");
        budgetRepository.save(testBudget);

        // Setup create DTO
        createDTO = new CreateGoalDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setName("Save for Car");
        createDTO.setTargetAmount(10000.0);

        // Setup update DTO
        updateDTO = new UpdateGoalDTO();
        updateDTO.setTargetAmount(15000.0);
    }

    @Test
    void testCreateGoal_Success() {
        // Act
        ResponseEntity<GoalResponseDTO> response = goalController.createGoal(createDTO);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(createDTO.getName(), response.getBody().getName());
        assertEquals(createDTO.getTargetAmount(), response.getBody().getTargetAmount());
        assertEquals(0.0, response.getBody().getCurrentAmount());
        assertNotNull(response.getBody().getCreatedAt());
        assertNotNull(response.getBody().getUpdatedAt());
    }

    @Test
    void testCreateGoal_InvalidUser() {
        // Arrange
        createDTO.setUserId(999L); // Non-existent user ID

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            goalController.createGoal(createDTO);
        });
    }

    @Test
    void testCreateGoal_DuplicateGoal() {
        // Arrange
        goalController.createGoal(createDTO); // Create first goal

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            goalController.createGoal(createDTO); // Attempt to create second goal
        });
    }

    @Test
    void testUpdateGoal_Success() {
        // Arrange
        ResponseEntity<GoalResponseDTO> created = goalController.createGoal(createDTO);
        updateDTO.setId(created.getBody().getId());

        // Act
        ResponseEntity<GoalResponseDTO> response = goalController.updateGoal(updateDTO);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(updateDTO.getTargetAmount(), response.getBody().getTargetAmount());
        assertEquals(created.getBody().getName(), response.getBody().getName());
    }

    @Test
    void testUpdateGoal_NotFound() {
        // Arrange
        updateDTO.setId(999L); // Non-existent goal ID

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            goalController.updateGoal(updateDTO);
        });
    }

    @Test
    void testGetGoalByUserId_Success() {
        // Arrange
        goalController.createGoal(createDTO);

        // Act
        ResponseEntity<GoalResponseDTO> response = goalController.getGoalByUserId(testUser.getId());

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(createDTO.getName(), response.getBody().getName());
        assertEquals(createDTO.getTargetAmount(), response.getBody().getTargetAmount());
    }

    @Test
    void testGetGoalByUserId_NotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            goalController.getGoalByUserId(999L);
        });
    }

    @Test
    void testGetGoalProgressByUserId_Success() {
        // Arrange
        goalController.createGoal(createDTO);

        // Act
        ResponseEntity<GoalProgressDTO> response = goalController.getGoalProgressByUserId(testUser.getId());

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(createDTO.getTargetAmount(), response.getBody().getTargetAmount());
        assertNotNull(response.getBody().getCurrentAmount());
        assertNotNull(response.getBody().getDifference());
        assertNotNull(response.getBody().getStatus());
        assertTrue(response.getBody().getProgressPercentage() >= 0);

        // Verify status is either AHEAD or BEHIND
        String status = response.getBody().getStatus();
        assertTrue("AHEAD".equals(status) || "BEHIND".equals(status));
    }

    @Test
    void testGetGoalProgressByUserId_NotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            goalController.getGoalProgressByUserId(999L);
        });
    }
}