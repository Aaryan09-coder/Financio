package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateGoalDTO;
import com.finpro.FinancePro.dto.Request.UpdateGoalDTO;
import com.finpro.FinancePro.dto.Response.GoalProgressDTO;
import com.finpro.FinancePro.dto.Response.GoalResponseDTO;
import com.finpro.FinancePro.exception.CustomAccessDeniedException;
import com.finpro.FinancePro.security.SecurityUtils;
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
    public ResponseEntity<GoalResponseDTO> createGoal(@Valid @RequestBody CreateGoalDTO createDTO) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new CustomAccessDeniedException("User must be authenticated");
        }

        GoalResponseDTO response = goalService.createGoal(createDTO, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<GoalResponseDTO> updateGoal(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateGoalDTO updateDTO) {
        // Check if the authenticated user matches the requested user ID
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to update this goal");
        }

        // Verify the goal belongs to the user before updating
        if (!goalService.isUserAuthorizedForGoal(updateDTO.getId(), userId)) {
            throw new CustomAccessDeniedException("You are not authorized to update this goal");
        }

        GoalResponseDTO response = goalService.updateGoal(updateDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<GoalResponseDTO> getCurrentUserGoal() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new CustomAccessDeniedException("User must be authenticated");
        }

        GoalResponseDTO response = goalService.getGoalByUserId(currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    public ResponseEntity<GoalProgressDTO> getCurrentUserGoalProgress() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new CustomAccessDeniedException("User must be authenticated");
        }

        GoalProgressDTO progress = goalService.calculateGoalProgressByUserId(currentUserId);
        return ResponseEntity.ok(progress);
    }
}