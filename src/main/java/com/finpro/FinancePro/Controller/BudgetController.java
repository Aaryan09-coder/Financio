package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateBudgetDTO;
import com.finpro.FinancePro.dto.Request.UpdateBudgetDTO;
import com.finpro.FinancePro.dto.Response.BudgetResponseDTO;
import com.finpro.FinancePro.repository.BudgetRepository;
import com.finpro.FinancePro.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    // Endpoint to create a new budget
    @PostMapping
    public ResponseEntity<BudgetResponseDTO> createBudget(@Valid @RequestBody CreateBudgetDTO createDTO) {
        BudgetResponseDTO budget = budgetService.createBudget(createDTO);
        return ResponseEntity.ok(budget);
    }

    // Endpoint to update an existing budget
    @PutMapping
    public ResponseEntity<BudgetResponseDTO> updateBudget(@Valid @RequestBody UpdateBudgetDTO updateDTO) {
        BudgetResponseDTO budget = budgetService.updateBudget(updateDTO);
        return ResponseEntity.ok(budget);
    }

    // Endpoint to retrieve a budget by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<BudgetResponseDTO> getBudgetByUser(@PathVariable Long userId) {
        BudgetResponseDTO budget = budgetService.getBudgetByUser(userId);
        return ResponseEntity.ok(budget);
    }

    // Endpoint to retrieve a budget by user ID and period
    @GetMapping("/user/{userId}/period/{period}")
    public ResponseEntity<BudgetResponseDTO> getBudgetByUserAndPeriod(
            @PathVariable Long userId,
            @PathVariable String period
    ) {
        BudgetResponseDTO budget = budgetService.getBudgetByUserAndPeriod(userId, period);
        return ResponseEntity.ok(budget);
    }


}
