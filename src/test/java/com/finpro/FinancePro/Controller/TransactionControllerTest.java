package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateTransactionDTO;
import com.finpro.FinancePro.dto.Request.UpdateTransactionDTO;
import com.finpro.FinancePro.entity.Transaction;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.repository.TransactionRepository;
import com.finpro.FinancePro.repository.UserRepository;
import com.finpro.FinancePro.security.SecurityUtils;
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
    private TransactionRepository transactionRepository;

    private User testUser;
    private Transaction testTransaction;

    @BeforeEach
    public void setUp() {
        // Create or find test user
        testUser = userRepository.findByEmail("test@example.com")
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail("test@example.com");
                    newUser.setFullName("Test User");
                    return userRepository.save(newUser);
                });

        // Set security context
        SecurityUtils.setTestUserId(testUser.getId());

        // Create test transaction
        testTransaction = new Transaction();
        testTransaction.setUser(testUser);
        testTransaction.setCategory("Salary");
        testTransaction.setAmount(5000.0);
        testTransaction.setType("INCOME");
        testTransaction.setDescription("Monthly Salary");
        testTransaction = transactionRepository.save(testTransaction);
    }

    @Test
    public void testGetTransactionsByUserId() {
        ResponseEntity<?> response = transactionController.getTransactionsByUserId(testUser.getId());

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        List<?> transactions = (List<?>) response.getBody();
        assertFalse(transactions.isEmpty());
    }

    @Test
    public void testCreateTransaction() {
        CreateTransactionDTO createDTO = new CreateTransactionDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setCategory("Freelance");
        createDTO.setAmount(2000.0);
        createDTO.setType("INCOME");
        createDTO.setDescription("Freelance Work");

        ResponseEntity<?> response = transactionController.createTransaction(createDTO);

        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    public void testGetTransactionsByDateRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);

        ResponseEntity<?> response = transactionController.getTransactionsByDateRange(
                testUser.getId(),
                yesterday,
                now.plusDays(1)
        );

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        List<?> transactions = (List<?>) response.getBody();
        assertFalse(transactions.isEmpty());
    }

    @Test
    public void testGetTransactionsByType() {
        ResponseEntity<?> response = transactionController.getTransactionsByType(
                testUser.getId(),
                "INCOME"
        );

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        List<?> transactions = (List<?>) response.getBody();
        assertFalse(transactions.isEmpty());
    }

    @Test
    public void testUpdateTransaction() {
        UpdateTransactionDTO updateDTO = new UpdateTransactionDTO();
        updateDTO.setId(testTransaction.getId());
        updateDTO.setCategory("Updated Salary");
        updateDTO.setAmount(6000.0);
        updateDTO.setType("INCOME");
        updateDTO.setDescription("Updated Monthly Salary");

        ResponseEntity<?> response = transactionController.updateTransaction(
                testUser.getId(),
                updateDTO
        );

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testDeleteTransaction() {
        Transaction transactionToDelete = new Transaction();
        transactionToDelete.setUser(testUser);
        transactionToDelete.setCategory("Temp Transaction");
        transactionToDelete.setAmount(100.0);
        transactionToDelete.setType("EXPENSE");
        transactionToDelete = transactionRepository.save(transactionToDelete);

        ResponseEntity<String> response = transactionController.deleteTransaction(transactionToDelete.getId());

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Transaction with ID " + transactionToDelete.getId() + " has been deleted successfully.", response.getBody());
    }
}