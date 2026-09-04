package com.sarthak.finance.controller;

import com.sarthak.finance.dto.request.CategoryRequest;
import com.sarthak.finance.dto.response.ApiResponse;
import com.sarthak.finance.dto.response.CategoryResponse;
import com.sarthak.finance.model.User;
import com.sarthak.finance.security.CustomUserDetails;
import com.sarthak.finance.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<CategoryResponse> categories = categoryService.getAllCategoriesForUser(user);
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        CategoryResponse response = categoryService.createCategory(request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{idOrName}")
    public ResponseEntity<ApiResponse> deleteCategory(
            @PathVariable String idOrName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        categoryService.deleteCategoryByIdOrName(idOrName, user);
        return ResponseEntity.ok(new ApiResponse("Category deleted successfully", true));
    }
}
