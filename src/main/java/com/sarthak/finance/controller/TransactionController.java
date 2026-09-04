package com.sarthak.finance.controller;

import com.sarthak.finance.dto.request.TransactionRequest;
import com.sarthak.finance.dto.request.UpdateTransactionRequest;
import com.sarthak.finance.dto.response.ApiResponse;
import com.sarthak.finance.dto.response.TransactionListResponse;
import com.sarthak.finance.dto.response.TransactionResponse;
import com.sarthak.finance.model.TransactionType;
import com.sarthak.finance.model.User;
import com.sarthak.finance.security.CustomUserDetails;
import com.sarthak.finance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Transactions", description = "Endpoints for creating, filtering, updating, and deleting income and expense transactions")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Create transaction", description = "Records a new income or expense transaction with category and date")
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        TransactionResponse response = transactionService.createTransaction(request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get transactions", description = "Retrieves all transactions for the authenticated user with optional filtering by date range, category, or transaction type")
    @GetMapping
    public ResponseEntity<TransactionListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TransactionType type,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<TransactionResponse> transactions;
        if (category != null && !category.trim().isEmpty()) {
            transactions = transactionService.getTransactions(user, startDate, endDate, categoryId, category, type);
        } else {
            transactions = transactionService.getTransactions(user, startDate, endDate, categoryId, type);
        }
        return ResponseEntity.ok(new TransactionListResponse(transactions));
    }

    @Operation(summary = "Get transaction by ID", description = "Fetches a specific transaction details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        TransactionResponse response = transactionService.getTransactionById(id, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update transaction", description = "Updates transaction amount, date, description, or category")
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        TransactionResponse response = transactionService.updateTransaction(id, request, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete transaction", description = "Deletes a transaction by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        transactionService.deleteTransaction(id, user);
        return ResponseEntity.ok(new ApiResponse("Transaction deleted successfully", true));
    }
}
