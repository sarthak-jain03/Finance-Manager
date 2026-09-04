package com.sarthak.finance.controller;

import com.sarthak.finance.dto.request.SavingsGoalRequest;
import com.sarthak.finance.dto.request.UpdateSavingsGoalRequest;
import com.sarthak.finance.dto.response.ApiResponse;
import com.sarthak.finance.dto.response.SavingsGoalResponse;
import com.sarthak.finance.model.User;
import com.sarthak.finance.security.CustomUserDetails;
import com.sarthak.finance.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Savings Goals", description = "Endpoints for tracking financial goals and calculating dynamic savings progress")
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @Operation(summary = "Create savings goal", description = "Creates a new savings goal with target date and target amount")
    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(
            @Valid @RequestBody SavingsGoalRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        SavingsGoalResponse response = savingsGoalService.createGoal(request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all savings goals", description = "Retrieves all savings goals for authenticated user with updated progress metrics")
    @GetMapping
    public ResponseEntity<List<SavingsGoalResponse>> getAllGoals(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<SavingsGoalResponse> goals = savingsGoalService.getAllGoals(user);
        return ResponseEntity.ok(goals);
    }

    @Operation(summary = "Get savings goal by ID", description = "Retrieves goal details by ID including current savings and percentage complete")
    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        SavingsGoalResponse response = savingsGoalService.getGoalById(id, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update savings goal", description = "Updates goal name, target amount, or target date by goal ID")
    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSavingsGoalRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        SavingsGoalResponse response = savingsGoalService.updateGoal(id, request, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete savings goal", description = "Deletes a savings goal by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        savingsGoalService.deleteGoal(id, user);
        return ResponseEntity.ok(new ApiResponse("Savings goal deleted successfully", true));
    }
}
