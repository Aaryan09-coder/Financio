package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateBudgetDTO;
import com.finpro.FinancePro.dto.Request.CreateGoalDTO;
import com.finpro.FinancePro.dto.Request.UpdateGoalDTO;
import com.finpro.FinancePro.dto.Response.GoalProgressDTO;
import com.finpro.FinancePro.dto.Response.GoalResponseDTO;
import com.finpro.FinancePro.entity.Goal;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.InvalidRequestException;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.GoalRepository;
import com.finpro.FinancePro.repository.UserRepository;
import com.finpro.FinancePro.security.SecurityUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class GoalServiceTest {

    @Autowired
    private GoalService goalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetService budgetService;

    private User testUser;
    private Long testUserId;  // Declare this here

    @BeforeEach
    public void setup() {
        // Create a test user with a unique name
        testUser = new User();
        testUser.setFullName("Test User " + System.currentTimeMillis());
        testUser.setEmail("test" + System.currentTimeMillis() + "@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId();

        // Create a budget for the test user
        CreateBudgetDTO createBudgetDTO = new CreateBudgetDTO();
        createBudgetDTO.setTotalAmount(5000.0);
        createBudgetDTO.setPeriod("MONTHLY");
        budgetService.createBudget(testUserId, createBudgetDTO);

        // Set the test user ID for security context
        SecurityUtils.setTestUserId(testUserId);
    }

    @Test
    public void testCreateGoal_Success() {
        CreateGoalDTO createDTO = new CreateGoalDTO();
        createDTO.setName("Test Savings Goal");
        createDTO.setTargetAmount(10000.0);

        GoalResponseDTO response = goalService.createGoal(createDTO, testUserId);

        assertNotNull(response);
        assertEquals("Test Savings Goal", response.getName());
        assertEquals(10000.0, response.getTargetAmount());
        assertNotNull(response.getId());
    }

    @Test
    public void testCreateGoal_DuplicateGoal() {
        // First goal creation
        CreateGoalDTO createDTO = new CreateGoalDTO();
        createDTO.setName("Test Savings Goal");
        createDTO.setTargetAmount(10000.0);
        goalService.createGoal(createDTO, testUserId);

        // Attempt to create a second goal for the same user
        CreateGoalDTO duplicateDTO = new CreateGoalDTO();
        duplicateDTO.setName("Another Goal");
        duplicateDTO.setTargetAmount(5000.0);

        assertThrows(InvalidRequestException.class, () -> {
            goalService.createGoal(duplicateDTO, testUserId);
        });
    }

    @Test
    public void testUpdateGoal_Success() {
        // First create a goal
        CreateGoalDTO createDTO = new CreateGoalDTO();
        createDTO.setName("Initial Goal");
        createDTO.setTargetAmount(10000.0);
        GoalResponseDTO createdGoal = goalService.createGoal(createDTO, testUserId);

        // Prepare update DTO
        UpdateGoalDTO updateDTO = new UpdateGoalDTO();
        updateDTO.setId(createdGoal.getId());
        updateDTO.setTargetAmount(15000.0);

        GoalResponseDTO updatedGoal = goalService.updateGoal(updateDTO);

        assertEquals(15000.0, updatedGoal.getTargetAmount());
    }

    @Test
    public void testGetGoalByUserId_Success() {
        // Create a goal first
        CreateGoalDTO createDTO = new CreateGoalDTO();
        createDTO.setName("Test Savings Goal");
        createDTO.setTargetAmount(10000.0);
        goalService.createGoal(createDTO, testUserId);

        // Retrieve the goal
        GoalResponseDTO retrievedGoal = goalService.getGoalByUserId(testUserId);

        assertNotNull(retrievedGoal);
        assertEquals("Test Savings Goal", retrievedGoal.getName());
        assertEquals(10000.0, retrievedGoal.getTargetAmount());
    }

    @Test
    public void testGetGoalByUserId_NotFound() {
        // Use a non-existent user ID
        Long nonExistentUserId = 99999L;

        assertThrows(ResourceNotFoundException.class, () -> {
            goalService.getGoalByUserId(nonExistentUserId);
        });
    }

    @Test
    public void testCalculateGoalProgressByUserId_Success() {
        // Create a goal first
        CreateGoalDTO createDTO = new CreateGoalDTO();
        createDTO.setName("Progress Goal");
        createDTO.setTargetAmount(10000.0);
        goalService.createGoal(createDTO, testUserId);

        // Calculate goal progress
        GoalProgressDTO progress = goalService.calculateGoalProgressByUserId(testUserId);

        assertNotNull(progress);
        assertEquals(10000.0, progress.getTargetAmount());
        assertTrue(progress.getCurrentAmount() >= 0);
        assertNotNull(progress.getStatus());
        assertTrue(progress.getProgressPercentage() >= 0);
    }

    @Test
    public void testIsUserAuthorizedForGoal_Success() {
        // Create a goal first
        CreateGoalDTO createDTO = new CreateGoalDTO();
        createDTO.setName("Authorization Goal");
        createDTO.setTargetAmount(10000.0);
        GoalResponseDTO createdGoal = goalService.createGoal(createDTO, testUserId);

        // Check authorization
        assertTrue(goalService.isUserAuthorizedForGoal(createdGoal.getId(), testUserId));
    }

    @Test
    public void testIsUserAuthorizedForGoal_Unauthorized() {
        // Create a goal first
        CreateGoalDTO createDTO = new CreateGoalDTO();
        createDTO.setName("Authorization Goal");
        createDTO.setTargetAmount(10000.0);
        GoalResponseDTO createdGoal = goalService.createGoal(createDTO, testUserId);

        // Check authorization with different user ID
        assertFalse(goalService.isUserAuthorizedForGoal(createdGoal.getId(), 99999L));
    }

    // Cleanup after tests
    @BeforeEach
    public void tearDown() {
        SecurityUtils.clearTestUserId();
    }
}