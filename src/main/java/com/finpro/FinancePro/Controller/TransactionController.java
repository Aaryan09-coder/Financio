package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateTransactionDTO;
import com.finpro.FinancePro.dto.Request.UpdateTransactionDTO;
import com.finpro.FinancePro.dto.Response.TransactionResponseDTO;
import com.finpro.FinancePro.entity.Transaction;
import com.finpro.FinancePro.exception.CustomAccessDeniedException;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.TransactionRepository;
import com.finpro.FinancePro.repository.UserRepository;
import com.finpro.FinancePro.security.SecurityUtils;
import com.finpro.FinancePro.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Get all transactions for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByUserId(@PathVariable Long userId) {
        // Check if the current user is authorized to access these transactions
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to view these transactions");
        }

        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByUserId(userId);
        if(transactions.isEmpty()) {
            throw new ResourceNotFoundException("No transactions found for user with ID: " + userId);
        }
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by date range
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByDateRange(
            @PathVariable Long userId,
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate) {

        // Check if the current user is authorized to access these transactions
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to view these transactions");
        }

        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate);
        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("No transactions found for user with ID: " + userId + " in the given date range.");
        }
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by type (INCOME, EXPENSE)
    @GetMapping("/user/{userId}/type")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByType(
            @PathVariable Long userId,
            @RequestParam("type") String type) {

        // Check if the current user is authorized to access these transactions
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to view these transactions");
        }

        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByType(userId, type);
        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("No " + type + " transactions found for user with ID: " + userId);
        }
        return ResponseEntity.ok(transactions);
    }

    // Create a new transaction
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody CreateTransactionDTO createTransactionDTO) {
        // Check if the current user is authorized to create a transaction for this user
        if (!SecurityUtils.isCurrentUserOrAdmin(createTransactionDTO.getUserId())) {
            throw new CustomAccessDeniedException("You are not authorized to create transactions for this user");
        }

        TransactionResponseDTO transactionResponse = transactionService.createTransaction(createTransactionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionResponse);
    }

    // Update an existing transaction
    @PutMapping("/user/{userId}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateTransactionDTO updateTransactionDTO) {

        // Check if the current user is authorized to access these transactions
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to update transactions for this user");
        }

        // First, get the transaction and verify it belongs to the user
        Transaction transaction = transactionRepository.findById(updateTransactionDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + updateTransactionDTO.getId()));

        // Verify the transaction belongs to the specified user
        if (!transaction.getUser().getId().equals(userId)) {
            throw new CustomAccessDeniedException("This transaction does not belong to the specified user");
        }

        TransactionResponseDTO updatedTransaction = transactionService.updateTransaction(updateTransactionDTO.getId(), updateTransactionDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    // Delete a transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable Long id) {
        // First, get the user ID from the current transaction
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        // Check if the current user is authorized to delete this transaction
        if (!SecurityUtils.isCurrentUserOrAdmin(transaction.getUser().getId())) {
            throw new CustomAccessDeniedException("You are not authorized to delete this transaction");
        }

        transactionService.deleteTransaction(id);
        return ResponseEntity.ok("Transaction with ID " + id + " has been deleted successfully.");
    }
}
