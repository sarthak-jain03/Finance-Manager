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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(
            @Valid @RequestBody SavingsGoalRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        SavingsGoalResponse response = savingsGoalService.createGoal(request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SavingsGoalResponse>> getAllGoals(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<SavingsGoalResponse> goals = savingsGoalService.getAllGoals(user);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        SavingsGoalResponse response = savingsGoalService.getGoalById(id, user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSavingsGoalRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        SavingsGoalResponse response = savingsGoalService.updateGoal(id, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        savingsGoalService.deleteGoal(id, user);
        return ResponseEntity.ok(new ApiResponse("Savings goal deleted successfully", true));
    }
}
