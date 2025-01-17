package com.finpro.FinancePro.Controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TransactionControllerTest {

    @Autowired
    private TransactionController transactionController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testUser;
    private Budget testBudget;
    private Transaction testTransaction;
    private CreateTransactionDTO createDTO;
    private UpdateTransactionDTO updateDTO;

    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = new User();
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);

        // Create and save test budget
        testBudget = new Budget();
        testBudget.setUser(testUser);
        testBudget.setSpentAmount(0.0);
        testBudget.setTotalAmount(1000.0);
        testBudget.setPeriod("2025-01");
        testBudget = budgetRepository.save(testBudget);

        // Setup create DTO
        createDTO = new CreateTransactionDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setCategory("Food");
        createDTO.setAmount(100.0);
        createDTO.setType("EXPENSE");
        createDTO.setDescription("Grocery shopping");

        // Create and save test transaction
        testTransaction = new Transaction();
        testTransaction.setUser(testUser);
        testTransaction.setCategory("Food");
        testTransaction.setAmount(100.0);
        testTransaction.setType("EXPENSE");
        testTransaction.setDescription("Grocery shopping");
        testTransaction.setCreatedAt(LocalDateTime.now());
        testTransaction = transactionRepository.save(testTransaction);

        // Setup update DTO
        updateDTO = new UpdateTransactionDTO();
        updateDTO.setId(testTransaction.getId());
        updateDTO.setCategory("Updated Food");
        updateDTO.setAmount(150.0);
        updateDTO.setType("EXPENSE");
        updateDTO.setDescription("Updated grocery shopping");
    }

    @Test
    void createTransaction_Success() {
        // Act
        ResponseEntity<TransactionResponseDTO> response = transactionController.createTransaction(createDTO);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCodeValue(), "Status code should be 200");

        TransactionResponseDTO transactionResponse = response.getBody();
        assertNotNull(transactionResponse, "Transaction response should not be null");
        assertEquals(createDTO.getCategory(), transactionResponse.getCategory(), "Category should match");
        assertEquals(createDTO.getAmount(), transactionResponse.getAmount(), "Amount should match");
        assertEquals(createDTO.getType(), transactionResponse.getType(), "Type should match");
        assertEquals(createDTO.getDescription(), transactionResponse.getDescription(), "Description should match");
        assertEquals(testUser.getId(), transactionResponse.getUserId(), "User ID should match");
    }

    @Test
    void getTransactionsByUserId_Success() {
        // Act
        ResponseEntity<List<TransactionResponseDTO>> response =
                transactionController.getTransactionsByUserId(testUser.getId());

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCodeValue(), "Status code should be 200");

        List<TransactionResponseDTO> transactions = response.getBody();
        assertNotNull(transactions, "Transactions list should not be null");
        assertFalse(transactions.isEmpty(), "Transactions list should not be empty");
        assertTrue(transactions.stream()
                .anyMatch(t -> t.getCategory().equals(testTransaction.getCategory())));
    }

    @Test
    void getTransactionsByUserId_UserNotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                        transactionController.getTransactionsByUserId(999L),
                "Should throw ResourceNotFoundException for non-existent user"
        );
    }

    @Test
    void getTransactionsByDateRange_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // Act
        ResponseEntity<List<TransactionResponseDTO>> response =
                transactionController.getTransactionsByDateRange(testUser.getId(), startDate, endDate);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCodeValue(), "Status code should be 200");

        List<TransactionResponseDTO> transactions = response.getBody();
        assertNotNull(transactions, "Transactions list should not be null");
        assertFalse(transactions.isEmpty(), "Transactions list should not be empty");
    }

    @Test
    void getTransactionsByType_Success() {
        // Act
        ResponseEntity<List<TransactionResponseDTO>> response =
                transactionController.getTransactionsByType(testUser.getId(), "EXPENSE");

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCodeValue(), "Status code should be 200");

        List<TransactionResponseDTO> transactions = response.getBody();
        assertNotNull(transactions, "Transactions list should not be null");
        assertFalse(transactions.isEmpty(), "Transactions list should not be empty");
        transactions.forEach(t -> assertEquals("EXPENSE", t.getType(), "All transactions should be expenses"));
    }

    @Test
    void updateTransaction_Success() {
        // Act
        ResponseEntity<TransactionResponseDTO> response =
                transactionController.updateTransaction(testTransaction.getId(), updateDTO);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCodeValue(), "Status code should be 200");

        TransactionResponseDTO updatedTransaction = response.getBody();
        assertNotNull(updatedTransaction, "Updated transaction should not be null");
        assertEquals(updateDTO.getCategory(), updatedTransaction.getCategory(), "Category should be updated");
        assertEquals(updateDTO.getAmount(), updatedTransaction.getAmount(), "Amount should be updated");
        assertEquals(updateDTO.getDescription(), updatedTransaction.getDescription(), "Description should be updated");
    }

    @Test
    void updateTransaction_NotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                        transactionController.updateTransaction(999L, updateDTO),
                "Should throw ResourceNotFoundException for non-existent transaction"
        );
    }

    @Test
    void deleteTransaction_Success() {
        // Act
        ResponseEntity<String> response = transactionController.deleteTransaction(testTransaction.getId());

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCodeValue(), "Status code should be 200");
        assertFalse(transactionRepository.existsById(testTransaction.getId()),
                "Transaction should be deleted from repository");
    }

    @Test
    void deleteTransaction_NotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                        transactionController.deleteTransaction(999L),
                "Should throw ResourceNotFoundException for non-existent transaction"
        );
    }
}