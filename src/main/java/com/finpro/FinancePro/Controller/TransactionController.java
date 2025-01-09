package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateTransactionDTO;
import com.finpro.FinancePro.dto.Request.UpdateTransactionDTO;
import com.finpro.FinancePro.dto.Response.TransactionResponseDTO;
import com.finpro.FinancePro.entity.Transaction;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.UserRepository;
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

    // Get all transactions for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByUserId(@PathVariable Long userId) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByUserId(userId);
        if(transactions.isEmpty()){
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
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByType(userId, type);
        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("No " + type + " transactions found for user with ID: " + userId);
        }
        return ResponseEntity.ok(transactions);
    }

    // Create a new transaction
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@RequestBody CreateTransactionDTO createTransactionDTO) {
        TransactionResponseDTO transactionResponse = transactionService.createTransaction(createTransactionDTO);
        return ResponseEntity.ok(transactionResponse);
    }

    // Update an existing transaction
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody UpdateTransactionDTO updateTransactionDTO) {
        TransactionResponseDTO updatedTransaction = transactionService.updateTransaction(id, updateTransactionDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    // Delete a transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok("Transaction with ID " + id + " has been deleted successfully.");
    }
}
