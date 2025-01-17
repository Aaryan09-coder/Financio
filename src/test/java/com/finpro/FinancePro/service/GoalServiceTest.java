package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateGoalDTO;
import com.finpro.FinancePro.dto.Request.CreateTransactionDTO;
import com.finpro.FinancePro.dto.Request.UpdateGoalDTO;
import com.finpro.FinancePro.dto.Response.GoalProgressDTO;
import com.finpro.FinancePro.dto.Response.GoalResponseDTO;
import com.finpro.FinancePro.entity.Budget;
import com.finpro.FinancePro.entity.Goal;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.InvalidRequestException;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.BudgetRepository;
import com.finpro.FinancePro.repository.GoalRepository;
import com.finpro.FinancePro.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class GoalServiceTest {

    @Autowired
    private GoalService goalService;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private User testUser;
    private Budget testBudget;
    private CreateTransactionDTO createDTO;

    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = new User();
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123"); // In real app, this should be encoded
        testUser = userRepository.save(testUser);

        // Create and save test budget
        testBudget = new Budget();
        testBudget.setUser(testUser);
        testBudget.setSpentAmount(0.0);
        testBudget.setTotalAmount(0.0);
        testBudget.setPeriod("2025-01"); // Example period value
        testBudget = budgetRepository.save(testBudget);

        // Setup create DTO
        createDTO = new CreateTransactionDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setCategory("Food");
        createDTO.setAmount(100.0);
        createDTO.setType("EXPENSE");
        createDTO.setDescription("Grocery shopping");
    }

    @Test
    void testCreateGoal() {
        // Create a goal for the test user
        CreateGoalDTO createGoalDTO = new CreateGoalDTO();
        createGoalDTO.setUserId(testUser.getId());
        createGoalDTO.setName("Save for Car");
        createGoalDTO.setTargetAmount(10000.0);

        GoalResponseDTO response = goalService.createGoal(createGoalDTO);

        assertNotNull(response);
        assertEquals("Save for Car", response.getName());
        assertEquals(10000.0, response.getTargetAmount());
        assertEquals(0.0, response.getCurrentAmount());
    }

    @Test
    void testUpdateGoal() {
        // Create and save a goal
        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setName("Save for Car");
        goal.setTargetAmount(10000.0);
        goal.setCurrentAmount(0.0);
        goal = goalRepository.save(goal);

        // Update the goal's target amount
        UpdateGoalDTO updateDTO = new UpdateGoalDTO();
        updateDTO.setId(goal.getId());
        updateDTO.setTargetAmount(15000.0);

        GoalResponseDTO response = goalService.updateGoal(updateDTO);

        assertNotNull(response);
        assertEquals(15000.0, response.getTargetAmount());
    }

    @Test
    void testGetGoalByUserId() {
        // Create and save a goal
        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setName("Save for Car");
        goal.setTargetAmount(10000.0);
        goal.setCurrentAmount(0.0);
        goalRepository.save(goal);

        // Fetch the goal by user ID
        GoalResponseDTO response = goalService.getGoalByUserId(testUser.getId());

        assertNotNull(response);
        assertEquals("Save for Car", response.getName());
        assertEquals(10000.0, response.getTargetAmount());
    }

    /*@Test
    void testCalculateGoalProgressByUserId() {
        // Create and save a goal
        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setName("Save for Car");
        goal.setTargetAmount(10000.0);
        goal.setCurrentAmount(5000.0);
        goalRepository.save(goal);

        // Fetch the goal progress
        GoalProgressDTO progressDTO = goalService.calculateGoalProgressByUserId(testUser.getId());

        assertNotNull(progressDTO);
        assertEquals(10000.0, progressDTO.getTargetAmount());
        assertEquals(5000.0, progressDTO.getCurrentAmount());
        assertEquals(-5000.0, progressDTO.getDifference());
        assertEquals("BEHIND", progressDTO.getStatus());
        assertEquals(50.0, progressDTO.getProgressPercentage());
    }
*/
    @Test
    void testCreateGoal_AlreadyExists() {
        // Create and save a goal
        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setName("Save for Car");
        goal.setTargetAmount(10000.0);
        goal.setCurrentAmount(0.0);
        goalRepository.save(goal);

        // Try creating a second goal for the same user
        CreateGoalDTO createGoalDTO = new CreateGoalDTO();
        createGoalDTO.setUserId(testUser.getId());
        createGoalDTO.setName("Save for House");
        createGoalDTO.setTargetAmount(20000.0);

        assertThrows(InvalidRequestException.class, () -> goalService.createGoal(createGoalDTO));
    }

    @Test
    void testGetGoalByUserId_NotFound() {
        // Try fetching a goal for a user with no goal
        assertThrows(ResourceNotFoundException.class, () -> goalService.getGoalByUserId(testUser.getId()));
    }
}
