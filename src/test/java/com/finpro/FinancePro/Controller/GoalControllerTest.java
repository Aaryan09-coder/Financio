package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateGoalDTO;
import com.finpro.FinancePro.dto.Request.UpdateGoalDTO;
import com.finpro.FinancePro.dto.Response.GoalProgressDTO;
import com.finpro.FinancePro.dto.Response.GoalResponseDTO;
import com.finpro.FinancePro.exception.CustomAccessDeniedException;
import com.finpro.FinancePro.security.SecurityUtils;
import com.finpro.FinancePro.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GoalControllerTest {

    @Mock
    private GoalService goalService;

    @InjectMocks
    private GoalController goalController;

    private CreateGoalDTO createGoalDTO;
    private UpdateGoalDTO updateGoalDTO;
    private GoalResponseDTO goalResponseDTO;
    private GoalProgressDTO goalProgressDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test data
        createGoalDTO = new CreateGoalDTO();
        createGoalDTO.setName("Save for Vacation");
        createGoalDTO.setTargetAmount(5000.00);

        updateGoalDTO = new UpdateGoalDTO();
        updateGoalDTO.setId(1L);
        updateGoalDTO.setTargetAmount(6000.00); // Only targetAmount is updated

        goalResponseDTO = new GoalResponseDTO();
        goalResponseDTO.setId(1L);
        goalResponseDTO.setName("Save for Vacation");
        goalResponseDTO.setTargetAmount(5000.00);
        goalResponseDTO.setCurrentAmount(2000.00);

        goalProgressDTO = new GoalProgressDTO();
        goalProgressDTO.setTargetAmount(5000.00);
        goalProgressDTO.setCurrentAmount(2000.00);
        goalProgressDTO.setDifference(3000.00);
        goalProgressDTO.setStatus("BEHIND");
        goalProgressDTO.setProgressPercentage(40.0);
    }

    @Test
    public void testCreateGoal_Success() {
        // Simulate authenticated user
        SecurityUtils.setTestUserId(1L);

        // Mock service behavior
        when(goalService.createGoal(createGoalDTO, 1L)).thenReturn(goalResponseDTO);

        // Call the controller method
        ResponseEntity<GoalResponseDTO> response = goalController.createGoal(createGoalDTO);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(goalResponseDTO.getId(), response.getBody().getId());
        assertEquals(goalResponseDTO.getName(), response.getBody().getName());

        // Verify service interaction
        verify(goalService, times(1)).createGoal(createGoalDTO, 1L);
    }

    @Test
    public void testCreateGoal_Unauthorized() {
        // Simulate unauthenticated user
        SecurityUtils.setTestUserId(null);

        // Verify that an exception is thrown
        CustomAccessDeniedException exception = assertThrows(CustomAccessDeniedException.class, () -> {
            goalController.createGoal(createGoalDTO);
        });

        assertEquals("User must be authenticated", exception.getMessage());

        // Verify no service interaction
        verify(goalService, never()).createGoal(any(), any());
    }

    @Test
    public void testUpdateGoal_Success() {
        // Simulate authenticated user
        SecurityUtils.setTestUserId(1L);

        // Mock service behavior
        when(goalService.isUserAuthorizedForGoal(1L, 1L)).thenReturn(true);
        when(goalService.updateGoal(updateGoalDTO)).thenReturn(goalResponseDTO);

        // Call the controller method
        ResponseEntity<GoalResponseDTO> response = goalController.updateGoal(1L, updateGoalDTO);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(goalResponseDTO.getId(), response.getBody().getId());
        assertEquals(goalResponseDTO.getTargetAmount(), response.getBody().getTargetAmount());

        // Verify service interaction
        verify(goalService, times(1)).isUserAuthorizedForGoal(1L, 1L);
        verify(goalService, times(1)).updateGoal(updateGoalDTO);
    }

    @Test
    public void testUpdateGoal_Unauthorized() {
        // Simulate unauthorized user
        SecurityUtils.setTestUserId(2L);

        // Mock service behavior
        when(goalService.isUserAuthorizedForGoal(1L, 2L)).thenReturn(false);

        // Verify that an exception is thrown
        CustomAccessDeniedException exception = assertThrows(CustomAccessDeniedException.class, () -> {
            goalController.updateGoal(1L, updateGoalDTO);
        });

        assertEquals("You are not authorized to update this goal", exception.getMessage());

        // Verify service interaction
        verify(goalService, times(1)).isUserAuthorizedForGoal(1L, 2L);
        verify(goalService, never()).updateGoal(any());
    }

    @Test
    public void testGetCurrentUserGoal_Success() {
        // Simulate authenticated user
        SecurityUtils.setTestUserId(1L);

        // Mock service behavior
        when(goalService.getGoalByUserId(1L)).thenReturn(goalResponseDTO);

        // Call the controller method
        ResponseEntity<GoalResponseDTO> response = goalController.getCurrentUserGoal();

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(goalResponseDTO.getId(), response.getBody().getId());
        assertEquals(goalResponseDTO.getName(), response.getBody().getName());

        // Verify service interaction
        verify(goalService, times(1)).getGoalByUserId(1L);
    }

    @Test
    public void testGetCurrentUserGoal_Unauthorized() {
        // Simulate unauthenticated user
        SecurityUtils.setTestUserId(null);

        // Verify that an exception is thrown
        CustomAccessDeniedException exception = assertThrows(CustomAccessDeniedException.class, () -> {
            goalController.getCurrentUserGoal();
        });

        assertEquals("User must be authenticated", exception.getMessage());

        // Verify no service interaction
        verify(goalService, never()).getGoalByUserId(any());
    }

    @Test
    public void testGetCurrentUserGoalProgress_Success() {
        // Simulate authenticated user
        SecurityUtils.setTestUserId(1L);

        // Mock service behavior
        when(goalService.calculateGoalProgressByUserId(1L)).thenReturn(goalProgressDTO);

        // Call the controller method
        ResponseEntity<GoalProgressDTO> response = goalController.getCurrentUserGoalProgress();

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(goalProgressDTO.getTargetAmount(), response.getBody().getTargetAmount());
        assertEquals(goalProgressDTO.getCurrentAmount(), response.getBody().getCurrentAmount());

        // Verify service interaction
        verify(goalService, times(1)).calculateGoalProgressByUserId(1L);
    }

    @Test
    public void testGetCurrentUserGoalProgress_Unauthorized() {
        // Simulate unauthenticated user
        SecurityUtils.setTestUserId(null);

        // Verify that an exception is thrown
        CustomAccessDeniedException exception = assertThrows(CustomAccessDeniedException.class, () -> {
            goalController.getCurrentUserGoalProgress();
        });

        assertEquals("User must be authenticated", exception.getMessage());

        // Verify no service interaction
        verify(goalService, never()).calculateGoalProgressByUserId(any());
    }
}