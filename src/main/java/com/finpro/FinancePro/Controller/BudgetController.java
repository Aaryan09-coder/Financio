package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateBudgetDTO;
import com.finpro.FinancePro.dto.Request.UpdateBudgetDTO;
import com.finpro.FinancePro.dto.Response.BudgetResponseDTO;
import com.finpro.FinancePro.exception.CustomAccessDeniedException;
import com.finpro.FinancePro.repository.BudgetRepository;
import com.finpro.FinancePro.security.SecurityUtils;
import com.finpro.FinancePro.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private SecurityUtils securityUtils;

    // Endpoint to create a new budget
    @PostMapping("/user/{userId}")
    public ResponseEntity<BudgetResponseDTO> createBudget(
            @PathVariable Long userId,
            @Valid @RequestBody CreateBudgetDTO createDTO) {
        // Check if the authenticated user matches the requested user ID
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to create a budget for this user");
        }

        BudgetResponseDTO budget = budgetService.createBudget(userId, createDTO);
        return ResponseEntity.ok(budget);
    }

    // Endpoint to update an existing budget
    @PutMapping("/user/{userId}")
    public ResponseEntity<BudgetResponseDTO> updateBudget(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateBudgetDTO updateDTO) {
        // Check if the authenticated user matches the requested user ID
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to update this budget");
        }

        // Verify the budget belongs to the user before updating
        if (!budgetService.isUserAuthorizedForBudget(updateDTO.getId(), userId)) {
            throw new CustomAccessDeniedException("You are not authorized to update this budget");
        }

        BudgetResponseDTO budget = budgetService.updateBudget(updateDTO);
        return ResponseEntity.ok(budget);
    }

    // Endpoint to retrieve a budget by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<BudgetResponseDTO> getBudgetByUser(@PathVariable Long userId) {
        // Check if the authenticated user matches the requested user ID
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to view this budget");
        }

        BudgetResponseDTO budget = budgetService.getBudgetByUser(userId);
        return ResponseEntity.ok(budget);
    }

    // Endpoint to retrieve a budget by user ID and period
    @GetMapping("/user/{userId}/period/{period}")
    public ResponseEntity<BudgetResponseDTO> getBudgetByUserAndPeriod(
            @PathVariable Long userId,
            @PathVariable String period) {
        // Check if the authenticated user matches the requested user ID
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to view this budget");
        }

        BudgetResponseDTO budget = budgetService.getBudgetByUserAndPeriod(userId, period);
        return ResponseEntity.ok(budget);
    }
}