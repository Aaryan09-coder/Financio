package com.finpro.FinancePro.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BudgetService budgetService;

    public boolean isUserAuthorizedForGoal(Long goalId, Long userId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId));
        return goal.getUser().getId().equals(userId);
    }

    private double calculateCurrentAmount(Long userId) {
        // Get income sum from transactions
        double incomeSum = transactionService.calculateIncomeSum(userId);

        // Get fresh remaining budget directly
        double remainingBudget = budgetService.getCurrentRemainingBudget(userId);

        return incomeSum + remainingBudget;
    }

    // Update this method in your GoalService class
    public GoalResponseDTO createGoal(CreateGoalDTO createGoalDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID " + userId));

        // Check if a goal already exists for the user
        Optional<Goal> existingGoal = goalRepository.findByUserId(userId);
        if (existingGoal.isPresent()) {
            throw new InvalidRequestException("A goal already exists for user ID " + userId);
        }

        Goal goal = new Goal();
        goal.setUser(user);
        goal.setName(createGoalDTO.getName());
        goal.setTargetAmount(createGoalDTO.getTargetAmount());
        goal.setCurrentAmount(0); // Initialize current amount to 0

        Goal savedGoal = goalRepository.save(goal);
        return convertToResponseDTO(savedGoal);
    }

    public GoalResponseDTO updateGoal(UpdateGoalDTO updateDTO) {
        Goal goal = goalRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + updateDTO.getId()));

        goal.setTargetAmount(updateDTO.getTargetAmount());
        goal.setCurrentAmount(calculateCurrentAmount(goal.getUser().getId())); // Calculate dynamic current amount

        Goal updatedGoal = goalRepository.save(goal);
        return convertToResponseDTO(updatedGoal);
    }

    public GoalResponseDTO getGoalByUserId(Long userId) {
        Goal goal = goalRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found for User ID: " + userId));

        // Update current amount before returning
        goal.setCurrentAmount(calculateCurrentAmount(userId));
        goal = goalRepository.save(goal);

        return convertToResponseDTO(goal);
    }

    //Method to get the progress of the Goal to maintain the BAR GRAPH
    public GoalProgressDTO calculateGoalProgressByUserId(Long userId) {
        Goal goal = goalRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found for user with ID: " + userId));

        // Get current amount using the existing calculation
        double currentAmount = calculateCurrentAmount(userId);

        GoalProgressDTO progressDTO = new GoalProgressDTO();
        progressDTO.setTargetAmount(goal.getTargetAmount());
        progressDTO.setCurrentAmount(currentAmount);

        // Calculate difference (positive means ahead, negative means behind)
        double difference = currentAmount - goal.getTargetAmount();
        progressDTO.setDifference(difference);

        // Set status based on difference
        progressDTO.setStatus(difference >= 0 ? "AHEAD" : "BEHIND");

        // Calculate progress percentage
        double progressPercentage = (currentAmount / goal.getTargetAmount()) * 100;
        progressDTO.setProgressPercentage(progressPercentage);

        return progressDTO;
    }


    private GoalResponseDTO convertToResponseDTO(Goal goal) {
        GoalResponseDTO response = new GoalResponseDTO();
        response.setId(goal.getId());
        response.setName(goal.getName());
        response.setTargetAmount(goal.getTargetAmount());
        response.setCurrentAmount(goal.getCurrentAmount());
        response.setCreatedAt(goal.getCreatedAt());
        response.setUpdatedAt(goal.getUpdatedAt());
        return response;
    }
}