package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateTransactionDTO;
import com.finpro.FinancePro.dto.Request.UpdateTransactionDTO;
import com.finpro.FinancePro.dto.Response.TransactionResponseDTO;
import com.finpro.FinancePro.entity.Transaction;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
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
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Transaction testTransaction;

    @BeforeEach
    public void setUp() {
        // Create a new test user if not exists
        testUser = userRepository.findByEmail("test@example.com")
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail("test@example.com");
                    newUser.setFullName("Test User");
                    // Set other necessary fields
                    return userRepository.save(newUser);
                });

        // Create a test transaction
        testTransaction = new Transaction();
        testTransaction.setUser(testUser);
        testTransaction.setCategory("Salary");
        testTransaction.setAmount(5000.0);
        testTransaction.setType("INCOME");
        testTransaction.setDescription("Monthly Salary");
        testTransaction = transactionRepository.save(testTransaction);
    }

    @Test
    public void testCreateTransaction() {
        CreateTransactionDTO createDTO = new CreateTransactionDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setCategory("Freelance");
        createDTO.setAmount(2000.0);
        createDTO.setType("INCOME");
        createDTO.setDescription("Freelance Work");

        TransactionResponseDTO responseDTO = transactionService.createTransaction(createDTO);

        assertNotNull(responseDTO);
        assertEquals("Freelance", responseDTO.getCategory());
        assertEquals(2000.0, responseDTO.getAmount());
        assertEquals("INCOME", responseDTO.getType());
    }

    @Test
    public void testGetTransactionsByUserId() {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByUserId(testUser.getId());

        assertFalse(transactions.isEmpty());
        assertTrue(transactions.stream().anyMatch(t -> t.getId().equals(testTransaction.getId())));
    }

    @Test
    public void testGetTransactionsByType() {
        List<TransactionResponseDTO> incomeTransactions = transactionService.getTransactionsByType(testUser.getId(), "INCOME");

        assertFalse(incomeTransactions.isEmpty());
        assertTrue(incomeTransactions.stream().allMatch(t -> "INCOME".equals(t.getType())));
    }

    @Test
    public void testGetTransactionsByDateRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);

        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByDateRange(
                testUser.getId(),
                yesterday,
                now.plusDays(1)
        );

        assertFalse(transactions.isEmpty());
    }

    @Test
    public void testGetTransaction() {
        TransactionResponseDTO transaction = transactionService.getTransaction(testTransaction.getId());

        assertNotNull(transaction);
        assertEquals(testTransaction.getId(), transaction.getId());
        assertEquals(testTransaction.getCategory(), transaction.getCategory());
    }

    @Test
    public void testUpdateTransaction() {
        UpdateTransactionDTO updateDTO = new UpdateTransactionDTO();
        updateDTO.setId(testTransaction.getId());
        updateDTO.setCategory("Updated Salary");
        updateDTO.setAmount(6000.0);
        updateDTO.setType("INCOME");
        updateDTO.setDescription("Updated Monthly Salary");

        TransactionResponseDTO updatedTransaction = transactionService.updateTransaction(testTransaction.getId(), updateDTO);

        assertNotNull(updatedTransaction);
        assertEquals("Updated Salary", updatedTransaction.getCategory());
        assertEquals(6000.0, updatedTransaction.getAmount());
    }

    @Test
    public void testDeleteTransaction() {
        Transaction transactionToDelete = new Transaction();
        transactionToDelete.setUser(testUser);
        transactionToDelete.setCategory("Temp Transaction");
        transactionToDelete.setAmount(100.0);
        transactionToDelete.setType("EXPENSE");
        transactionToDelete = transactionRepository.save(transactionToDelete);

        transactionService.deleteTransaction(transactionToDelete.getId());

        assertFalse(transactionRepository.existsById(transactionToDelete.getId()));
    }

    @Test
    public void testCalculateIncomeSum() {
        double totalIncome = transactionService.calculateIncomeSum(testUser.getId());

        assertTrue(totalIncome >= 0);
    }

    @Test
    public void testGetTransactionNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.getTransaction(Long.MAX_VALUE);
        });
    }
}