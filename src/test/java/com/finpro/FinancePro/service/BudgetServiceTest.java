package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateBudgetDTO;
import com.finpro.FinancePro.dto.Request.UpdateBudgetDTO;
import com.finpro.FinancePro.dto.Response.BudgetResponseDTO;
import com.finpro.FinancePro.entity.Transaction;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.InvalidRequestException;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.BudgetRepository;
import com.finpro.FinancePro.repository.TransactionRepository;
import com.finpro.FinancePro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BudgetServiceTest {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testuser;
    private CreateBudgetDTO createDTO;
    private UpdateBudgetDTO updateDTO;

    @BeforeEach
    void setUp(){

        //create and save test user;
        testuser = new User();
        testuser.setFullName("Test User");
        testuser.setEmail("test@example.com");
        testuser.setPassword("password123");
        testuser = userRepository.save(testuser);

        //Setup create DTO
        createDTO = new CreateBudgetDTO();
        createDTO.setUserId(testuser.getId());
        createDTO.setPeriod("Monthly");
        createDTO.setTotalAmount(1000.0);

        //Setup update DTO
        updateDTO = new UpdateBudgetDTO();
        updateDTO.setTotalAmount(1500.0);
    }

    @Test
    void testCreateBudget_Success() {
        //Act
        BudgetResponseDTO response = budgetService.createBudget(createDTO);

        //Assert
        assertNotNull(response);
        assertEquals(createDTO.getTotalAmount(), response.getTotalAmount());
        assertEquals(createDTO.getPeriod(), response.getPeriod());
        assertEquals(0.0, response.getSpentAmount()); //Initially no expense
        assertEquals(createDTO.getTotalAmount(), response.getRemainingBudget());
    }

    @Test
    void testCreateBudget_InvalidUser(){
        //Arrange
        createDTO.setUserId(999L); //Non-existent userId

        //Act & Assert
        assertThrows(ResourceNotFoundException.class, ()-> {
            budgetService.createBudget(createDTO);
        });
    }

    @Test
    void testCreateBudget_DuplicateUser() {
        // Arrange
        budgetService.createBudget(createDTO); // Create first budget

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            budgetService.createBudget(createDTO); // Attempt to create second budget
        });
    }

    @Test
    void testUpdateBudget_Success() {
        // Arrange
        BudgetResponseDTO created = budgetService.createBudget(createDTO);
        updateDTO.setId(created.getId());

        // Act
        BudgetResponseDTO updated = budgetService.updateBudget(updateDTO);

        // Assert
        assertNotNull(updated);
        assertEquals(updateDTO.getTotalAmount(), updated.getTotalAmount());
        assertEquals(created.getPeriod(), updated.getPeriod());
    }

    @Test
    void testUpdateBudget_NotFound() {
        // Arrange
        updateDTO.setId(999L); // Non-existent budget ID

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            budgetService.updateBudget(updateDTO);
        });
    }

    @Test
    void testGetBudgetByUser_Success() {
        // Arrange
        budgetService.createBudget(createDTO);

        // Act
        BudgetResponseDTO response = budgetService.getBudgetByUser(testuser.getId());

        // Assert
        assertNotNull(response);
        assertEquals(createDTO.getTotalAmount(), response.getTotalAmount());
        assertEquals(createDTO.getPeriod(), response.getPeriod());
    }

    @Test
    void testGetBudgetByUser_NotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            budgetService.getBudgetByUser(999L);
        });
    }

    @Test
    void testGetBudgetByUserAndPeriod_NotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            budgetService.getBudgetByUserAndPeriod(testuser.getId(), "YEARLY");
        });
    }

    @Test
    void testCalculateSpentAmount() {
        // Arrange
        BudgetResponseDTO budget = budgetService.createBudget(createDTO);

        // Create test transactions
        Transaction expense1 = new Transaction();
        expense1.setUser(testuser);
        expense1.setAmount(200.0);
        expense1.setType("EXPENSE");
        expense1.setCategory("Food");

        Transaction expense2 = new Transaction();
        expense2.setUser(testuser);
        expense2.setAmount(300.0);
        expense2.setType("EXPENSE");
        expense2.setCategory("Transport");

        Transaction income = new Transaction();
        income.setUser(testuser);
        income.setAmount(1000.0);
        income.setType("INCOME");
        income.setCategory("Salary");

        transactionRepository.saveAll(List.of(expense1, expense2, income));

        // Act
        BudgetResponseDTO response = budgetService.getBudgetByUser(testuser.getId());

        // Assert
        assertEquals(500.0, response.getSpentAmount()); // 200 + 300
        assertEquals(500.0, response.getRemainingBudget()); // 1000 - 500
    }

    @Test
    void testGetCurrentRemainingBudget() {
        // Arrange
        budgetService.createBudget(createDTO);

        Transaction expense = new Transaction();
        expense.setUser(testuser);
        expense.setAmount(400.0);
        expense.setType("EXPENSE");
        expense.setCategory("Shopping");
        transactionRepository.save(expense);

        // Act
        double remainingBudget = budgetService.getCurrentRemainingBudget(testuser.getId());

        // Assert
        assertEquals(600.0, remainingBudget); // 1000 - 400
    }

    @Test
    void testGetCurrentRemainingBudget_NotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            budgetService.getCurrentRemainingBudget(999L);
        });
    }
}
