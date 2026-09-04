package com.sarthak.finance.controller;

import com.sarthak.finance.dto.request.CategoryRequest;
import com.sarthak.finance.dto.response.ApiResponse;
import com.sarthak.finance.dto.response.CategoryResponse;
import com.sarthak.finance.model.User;
import com.sarthak.finance.security.CustomUserDetails;
import com.sarthak.finance.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Categories", description = "Endpoints for retrieving default & custom categories and managing user categories")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get all categories", description = "Retrieves all system default categories and custom user-created categories")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<CategoryResponse> categories = categoryService.getAllCategoriesForUser(user);
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Create custom category", description = "Creates a new custom income or expense category for the authenticated user")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        CategoryResponse response = categoryService.createCategory(request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete custom category", description = "Deletes a custom category by ID or name if owned by the authenticated user")
    @DeleteMapping("/{idOrName}")
    public ResponseEntity<ApiResponse> deleteCategory(
            @PathVariable String idOrName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        categoryService.deleteCategoryByIdOrName(idOrName, user);
        return ResponseEntity.ok(new ApiResponse("Category deleted successfully", true));
    }
}
