package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateTransactionDTO;
import com.finpro.FinancePro.dto.Request.UpdateTransactionDTO;
import com.finpro.FinancePro.dto.Response.TransactionResponseDTO;
import com.finpro.FinancePro.entity.Budget;
import com.finpro.FinancePro.entity.Transaction;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.BudgetRepository;
import com.finpro.FinancePro.repository.TransactionRepository;
import com.finpro.FinancePro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    // Calculate the Sum of all INCOME Transactions
    public double calculateIncomeSum(Long userId) {
        List<Transaction> incomeTransactions = transactionRepository.findByUserIdAndType(userId, "INCOME");
        return incomeTransactions.stream().mapToDouble(Transaction::getAmount).sum();
    }

    // Get all transactions for a user
    public List<TransactionResponseDTO> getTransactionsByUserId(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("No transactions found for user with ID: " + userId);
        }
        return transactions.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    // Get transactions by type (INCOME/EXPENSE) for a user
    public List<TransactionResponseDTO> getTransactionsByType(Long userId, String type) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndType(userId, type.toUpperCase());
        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("No " + type + " transactions found for user with ID: " + userId);
        }
        return transactions.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    // Get transactions for a user within a date range
    public List<TransactionResponseDTO> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate);
        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("No transactions found for user with ID: " + userId + " in the given date range.");
        }
        return transactions.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }


    public TransactionResponseDTO createTransaction(CreateTransactionDTO createDTO) {
        // Fetch the user
        User user = userRepository.findById(createDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + createDTO.getUserId()));

        // Create the transaction entity
        Transaction transaction = new Transaction();
        transaction.setCategory(createDTO.getCategory());
        transaction.setAmount(createDTO.getAmount());
        transaction.setType(createDTO.getType());
        transaction.setDescription(createDTO.getDescription());
        transaction.setUser(user); // Setting the user entity
        transaction.setCreatedAt(LocalDateTime.now()); // Optional: setting creation timestamp

        //save the transaction
        Transaction savedTransaction = transactionRepository.save(transaction);

        //Changes for storing spent amount
        // Update the spent amount in the budget if the transaction is an expense
        if ("EXPENSE".equalsIgnoreCase(createDTO.getType())) {
            Budget budget = budgetRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("No budget found for user ID: " + user.getId()));

            // Increment the spent amount
            budget.setSpentAmount(budget.getSpentAmount() + createDTO.getAmount());
            budgetRepository.save(budget);
        }

        return convertToResponseDTO(savedTransaction);
    }


    // Update an existing transaction
    public TransactionResponseDTO updateTransaction(Long id, UpdateTransactionDTO updateTransactionDTO) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
        transaction.setCategory(updateTransactionDTO.getCategory());
        transaction.setAmount(updateTransactionDTO.getAmount());
        transaction.setType(updateTransactionDTO.getType());
        transaction.setDescription(updateTransactionDTO.getDescription());
        Transaction updatedTransaction = transactionRepository.save(transaction);
        return convertToResponseDTO(updatedTransaction);
    }

    // Delete a transaction
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction not found with ID: " + id);
        }

        transactionRepository.deleteById(id);
    }


    // Helper method to convert Transaction entity to TransactionResponseDTO
    private TransactionResponseDTO convertToResponseDTO(Transaction transaction) {
        TransactionResponseDTO responseDTO = new TransactionResponseDTO();
        responseDTO.setId(transaction.getId());
        responseDTO.setCategory(transaction.getCategory());
        responseDTO.setAmount(transaction.getAmount());
        responseDTO.setType(transaction.getType());
        responseDTO.setDescription(transaction.getDescription());
        responseDTO.setUserId(transaction.getUser().getId()); // Ensure this is present
        return responseDTO;
    }
}
