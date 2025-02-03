package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateBudgetDTO;
import com.finpro.FinancePro.dto.Request.UpdateBudgetDTO;
import com.finpro.FinancePro.dto.Response.BudgetResponseDTO;
import com.finpro.FinancePro.entity.Budget;
import com.finpro.FinancePro.entity.Transaction;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.InvalidRequestException;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.BudgetRepository;
import com.finpro.FinancePro.repository.TransactionRepository;
import com.finpro.FinancePro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public BudgetResponseDTO createBudget(Long userId, CreateBudgetDTO createDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID " + userId));

        // Check if a budget already exists for the user
        budgetRepository.findByUserId(userId).ifPresent(existingBudget -> {
            throw new InvalidRequestException("A budget already exists for this user.");
        });

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setTotalAmount(createDTO.getTotalAmount());
        budget.setSpentAmount(calculateSpentAmount(userId)); // Calculate spent amount
        budget.setPeriod(createDTO.getPeriod());

        Budget savedBudget = budgetRepository.save(budget);
        return convertToResponseDTO(savedBudget);
    }

    public BudgetResponseDTO updateBudget(UpdateBudgetDTO updateDTO) {
        Budget budget = budgetRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID " + updateDTO.getId()));

        budget.setTotalAmount(updateDTO.getTotalAmount());
        budget.setSpentAmount(calculateSpentAmount(budget.getUser().getId())); // Recalculate spent amount

        Budget updatedBudget = budgetRepository.save(budget);
        return convertToResponseDTO(updatedBudget);
    }

    public boolean isUserAuthorizedForBudget(Long budgetId, Long userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID " + budgetId));
        return budget.getUser().getId().equals(userId);
    }

    public BudgetResponseDTO getBudgetByUser(Long userId) {
        Budget budget = budgetRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No budget found for user ID " + userId));

        // Dynamically calculate the spent amount
        double spentAmount = calculateSpentAmount(userId);

        // Update the spent amount in the budget and save it to the database
        budget.setSpentAmount(spentAmount);
        budgetRepository.save(budget); //save the updated spent amount in database

        return convertToResponseDTO(budget);
    }

    public BudgetResponseDTO getBudgetByUserAndPeriod(Long userId, String period) {
        Budget budget = budgetRepository.findByUserIdAndPeriod(userId, period)
                .orElseThrow(() -> new ResourceNotFoundException("No budget found for user ID " + userId + " and period " + period));
        budget.setSpentAmount(calculateSpentAmount(userId)); // Dynamically calculate spent amount
        return convertToResponseDTO(budget);
    }

    private double calculateSpentAmount(Long userId) {
        // Retrieve all expense transactions for the user and sum their amounts
        List<Transaction> expenses = transactionRepository.findByUserIdAndType(userId, "EXPENSE");
        return expenses.stream().mapToDouble(Transaction::getAmount).sum();
    }

    public double getCurrentRemainingBudget(Long userId) {
        Budget budget = budgetRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No budget found for user ID " + userId));

        // Calculate fresh spent amount
        double spentAmount = calculateSpentAmount(userId);

        // Calculate remaining budget
        return budget.getTotalAmount() - spentAmount;
    }

    private BudgetResponseDTO convertToResponseDTO(Budget budget) {
        BudgetResponseDTO responseDTO = new BudgetResponseDTO();
        responseDTO.setId(budget.getId());
        responseDTO.setTotalAmount(budget.getTotalAmount());
        responseDTO.setSpentAmount(budget.getSpentAmount());
        responseDTO.setRemainingBudget(budget.getTotalAmount() - budget.getSpentAmount()); // Calculate remaining budget
        responseDTO.setPeriod(budget.getPeriod());
        return responseDTO;
    }

}