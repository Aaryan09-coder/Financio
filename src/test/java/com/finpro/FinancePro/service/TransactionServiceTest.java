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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

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
    void testCreateTransaction_Success() {
        // Act
        TransactionResponseDTO response = transactionService.createTransaction(createDTO);

        // Assert
        assertNotNull(response, "Transaction response should not be null");
        assertEquals(createDTO.getCategory(), response.getCategory(), "Category should match");
        assertEquals(createDTO.getAmount(), response.getAmount(), "Amount should match");
        assertEquals(createDTO.getType(), response.getType(), "Type should match");
        assertEquals(createDTO.getDescription(), response.getDescription(), "Description should match");
        assertEquals(testUser.getId(), response.getUserId(), "User ID should match");
    }

    @Test
    void testCreateTransaction_InvalidUser() {
        // Arrange
        createDTO.setUserId(999L); // Non-existent user ID

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.createTransaction(createDTO);
        }, "Should throw ResourceNotFoundException for invalid user ID");
    }

    @Test
    void testCreateTransaction_UpdatesBudget() {
        // Arrange
        double initialSpentAmount = testBudget.getSpentAmount();

        // Act
        transactionService.createTransaction(createDTO);

        // Assert
        Budget updatedBudget = budgetRepository.findByUserId(testUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        assertEquals(
                initialSpentAmount + createDTO.getAmount(),
                updatedBudget.getSpentAmount(),
                "Budget spent amount should be updated"
        );
    }

    @Test
    void testCreateTransaction_WithIncome() {
        // Arrange
        createDTO.setType("INCOME");
        createDTO.setCategory("Salary");
        double initialSpentAmount = testBudget.getSpentAmount();

        // Act
        TransactionResponseDTO response = transactionService.createTransaction(createDTO);

        // Assert
        assertNotNull(response, "Transaction response should not be null");
        assertEquals("INCOME", response.getType(), "Type should be INCOME");

        Budget updatedBudget = budgetRepository.findByUserId(testUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        assertEquals(
                initialSpentAmount,
                updatedBudget.getSpentAmount(),
                "Budget spent amount should not change for income transactions"
        );
    }

    @Test
    void testCalculateIncomeSum() {
        // Arrange: Create and save income transactions
        Transaction incomeTransaction1 = new Transaction();
        incomeTransaction1.setUser(testUser);
        incomeTransaction1.setCategory("Salary");
        incomeTransaction1.setAmount(100.0);
        incomeTransaction1.setType("INCOME");
        incomeTransaction1.setDescription("Monthly salary");
        transactionRepository.save(incomeTransaction1);

        Transaction incomeTransaction2 = new Transaction();
        incomeTransaction2.setUser(testUser);
        incomeTransaction2.setCategory("Freelance");
        incomeTransaction2.setAmount(200.0);
        incomeTransaction2.setType("INCOME");
        incomeTransaction2.setDescription("Freelance project payment");
        transactionRepository.save(incomeTransaction2);

        // Act: Calculate the income sum
        double incomeSum = transactionService.calculateIncomeSum(testUser.getId());

        // Assert: Verify the income sum is correct
        assertEquals(300.0, incomeSum, "Income sum should match the total of income transactions");
    }

    @Test
    void testGetTransactionsByUserId() {
        // Arrange
        transactionService.createTransaction(createDTO); // Add a transaction

        // Act
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByUserId(testUser.getId());

        // Assert
        assertNotNull(transactions, "Transactions list should not be null");
        assertFalse(transactions.isEmpty(), "Transactions list should not be empty");
        assertEquals(1, transactions.size(), "There should be exactly one transaction");
    }

    @Test
    void testGetTransactionsByType() {
        // Arrange
        transactionService.createTransaction(createDTO); // Add an expense transaction

        // Act
        List<TransactionResponseDTO> expenses = transactionService.getTransactionsByType(testUser.getId(), "EXPENSE");

        // Assert
        assertNotNull(expenses, "Expenses list should not be null");
        assertFalse(expenses.isEmpty(), "Expenses list should not be empty");
        assertEquals("EXPENSE", expenses.get(0).getType(), "Transaction type should be EXPENSE");
    }

    @Test
    void testGetTransactionsByDateRange() {
        // Arrange
        transactionService.createTransaction(createDTO);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now.plusDays(1);

        // Act
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByDateRange(
                testUser.getId(), startDate, endDate);

        // Assert
        assertNotNull(transactions, "Transactions list should not be null");
        assertFalse(transactions.isEmpty(), "Transactions list should not be empty");
    }

    @Test
    void testUpdateTransaction(){

        TransactionResponseDTO createdTransaction = transactionService.createTransaction(createDTO);
        UpdateTransactionDTO updateTransactionDTO = new UpdateTransactionDTO();

        updateTransactionDTO.setCategory("Updated Category");
        updateTransactionDTO.setAmount(200.0);
        updateTransactionDTO.setType("EXPENSE");
        updateTransactionDTO.setDescription("Updated description");

        TransactionResponseDTO updatedTransaction = transactionService.updateTransaction(createdTransaction.getId(), updateTransactionDTO);

        assertNotNull(updatedTransaction, "Updated transaction should not be null");
        assertEquals(updateTransactionDTO.getCategory(), updatedTransaction.getCategory(), "Category should match updated value");
        assertEquals(updateTransactionDTO.getAmount(), updatedTransaction.getAmount(), "Amount should match updated value");
    }

    @Test
    void testDeleteTransaction(){

        TransactionResponseDTO createdTransaction = transactionService.createTransaction(createDTO);

        assertDoesNotThrow(() -> transactionService.deleteTransaction(createdTransaction.getId()), "Transaction should be deleted without exceptions");

        assertThrows(ResourceNotFoundException.class, ()->{
            transactionService.deleteTransaction(createdTransaction.getId());
        }, "Should throw ResourceNotFoundException for non-existent transaction ID");
    }
}