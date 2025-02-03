package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateBudgetDTO;
import com.finpro.FinancePro.dto.Request.UpdateBudgetDTO;
import com.finpro.FinancePro.dto.Response.BudgetResponseDTO;
import com.finpro.FinancePro.entity.Provider;
import com.finpro.FinancePro.entity.Transaction;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.InvalidRequestException;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.BudgetRepository;
import com.finpro.FinancePro.repository.TransactionRepository;
import com.finpro.FinancePro.repository.UserRepository;
import com.finpro.FinancePro.security.SecurityUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    private User testUser;

    @BeforeEach
    public void setUp() {
        // Create a test user with a unique full name
        testUser = new User();
        testUser.setEmail("test" + System.currentTimeMillis() + "@example.com");
        testUser.setFullName("Test User " + System.currentTimeMillis());
        testUser.setPassword("password");
        testUser.setProvider(Provider.SELF);
        testUser = userRepository.save(testUser);

        // Set the test user ID for SecurityUtils
        SecurityUtils.setTestUserId(testUser.getId());
    }

    @Test
    public void testCreateBudget() {
        CreateBudgetDTO createDTO = new CreateBudgetDTO();
        createDTO.setTotalAmount(1000.0);
        createDTO.setPeriod("MONTHLY");

        BudgetResponseDTO budgetResponse = budgetService.createBudget(testUser.getId(), createDTO);

        assertNotNull(budgetResponse);
        assertEquals(1000.0, budgetResponse.getTotalAmount());
        assertEquals("MONTHLY", budgetResponse.getPeriod());
        assertEquals(0.0, budgetResponse.getSpentAmount());
    }

    @Test
    public void testCreateBudgetThrowsExceptionWhenBudgetAlreadyExists() {
        CreateBudgetDTO createDTO = new CreateBudgetDTO();
        createDTO.setTotalAmount(1000.0);
        createDTO.setPeriod("MONTHLY");
        budgetService.createBudget(testUser.getId(), createDTO);

        CreateBudgetDTO duplicateDTO = new CreateBudgetDTO();
        duplicateDTO.setTotalAmount(2000.0);
        duplicateDTO.setPeriod("YEARLY");

        assertThrows(InvalidRequestException.class, () -> {
            budgetService.createBudget(testUser.getId(), duplicateDTO);
        });
    }

    @Test
    public void testUpdateBudget() {
        CreateBudgetDTO createDTO = new CreateBudgetDTO();
        createDTO.setTotalAmount(1000.0);
        createDTO.setPeriod("MONTHLY");
        BudgetResponseDTO createdBudget = budgetService.createBudget(testUser.getId(), createDTO);

        UpdateBudgetDTO updateDTO = new UpdateBudgetDTO();
        updateDTO.setId(createdBudget.getId());
        updateDTO.setTotalAmount(1500.0);

        BudgetResponseDTO updatedBudget = budgetService.updateBudget(updateDTO);

        assertNotNull(updatedBudget);
        assertEquals(1500.0, updatedBudget.getTotalAmount());
    }

    @Test
    public void testGetBudgetByUser() {
        CreateBudgetDTO createDTO = new CreateBudgetDTO();
        createDTO.setTotalAmount(1000.0);
        createDTO.setPeriod("MONTHLY");
        budgetService.createBudget(testUser.getId(), createDTO);

        BudgetResponseDTO retrievedBudget = budgetService.getBudgetByUser(testUser.getId());

        assertNotNull(retrievedBudget);
        assertEquals(1000.0, retrievedBudget.getTotalAmount());
        assertEquals("MONTHLY", retrievedBudget.getPeriod());
    }

    @Test
    public void testGetBudgetByUserAndPeriod() {
        CreateBudgetDTO createDTO = new CreateBudgetDTO();
        createDTO.setTotalAmount(1000.0);
        createDTO.setPeriod("MONTHLY");
        budgetService.createBudget(testUser.getId(), createDTO);

        BudgetResponseDTO retrievedBudget = budgetService.getBudgetByUserAndPeriod(testUser.getId(), "MONTHLY");

        assertNotNull(retrievedBudget);
        assertEquals(1000.0, retrievedBudget.getTotalAmount());
        assertEquals("MONTHLY", retrievedBudget.getPeriod());
    }

    @Test
    public void testCalculateSpentAmount() {
        CreateBudgetDTO createDTO = new CreateBudgetDTO();
        createDTO.setTotalAmount(1000.0);
        createDTO.setPeriod("MONTHLY");
        budgetService.createBudget(testUser.getId(), createDTO);

        Transaction transaction1 = new Transaction();
        transaction1.setUser(testUser);
        transaction1.setAmount(100.0);
        transaction1.setType("EXPENSE");
        transaction1.setCategory("UTILITIES");
        transactionRepository.save(transaction1);

        Transaction transaction2 = new Transaction();
        transaction2.setUser(testUser);
        transaction2.setAmount(200.0);
        transaction2.setType("EXPENSE");
        transaction2.setCategory("FOOD");
        transactionRepository.save(transaction2);

        double remainingBudget = budgetService.getCurrentRemainingBudget(testUser.getId());

        assertEquals(700.0, remainingBudget);
    }

    @Test
    public void testGetBudgetByUserThrowsExceptionWhenNobudgetExists() {
        assertThrows(ResourceNotFoundException.class, () -> {
            budgetService.getBudgetByUser(testUser.getId());
        });
    }

    @Test
    public void testGetBudgetByUserAndPeriodThrowsExceptionWhenNoBudgetExists() {
        assertThrows(ResourceNotFoundException.class, () -> {
            budgetService.getBudgetByUserAndPeriod(testUser.getId(), "YEARLY");
        });
    }

    @Test
    public void testIsUserAuthorizedForBudget() {
        CreateBudgetDTO createDTO = new CreateBudgetDTO();
        createDTO.setTotalAmount(1000.0);
        createDTO.setPeriod("MONTHLY");
        BudgetResponseDTO createdBudget = budgetService.createBudget(testUser.getId(), createDTO);

        boolean isAuthorized = budgetService.isUserAuthorizedForBudget(createdBudget.getId(), testUser.getId());

        assertTrue(isAuthorized);
    }

    @AfterEach
    public void tearDown() {
        SecurityUtils.clearTestUserId();
    }
}