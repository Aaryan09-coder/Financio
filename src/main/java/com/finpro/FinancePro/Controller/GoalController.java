package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateGoalDTO;
import com.finpro.FinancePro.dto.Request.UpdateGoalDTO;
import com.finpro.FinancePro.dto.Response.GoalProgressDTO;
import com.finpro.FinancePro.dto.Response.GoalResponseDTO;
import com.finpro.FinancePro.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @PostMapping
    public ResponseEntity<GoalResponseDTO> createGoal(@RequestBody @Valid CreateGoalDTO createDTO) {
        GoalResponseDTO response = goalService.createGoal(createDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<GoalResponseDTO> updateGoal(@RequestBody @Valid UpdateGoalDTO updateDTO) {
        GoalResponseDTO response = goalService.updateGoal(updateDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<GoalResponseDTO> getGoalByUserId(@PathVariable Long userId) {
        GoalResponseDTO response = goalService.getGoalByUserId(userId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/user/{userId}/progress")
    public ResponseEntity<GoalProgressDTO> getGoalProgressByUserId(@PathVariable Long userId) {
        GoalProgressDTO progress = goalService.calculateGoalProgressByUserId(userId);
        return ResponseEntity.ok(progress);
    }
}
