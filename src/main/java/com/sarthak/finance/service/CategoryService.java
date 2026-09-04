package com.sarthak.finance.service;

import com.sarthak.finance.dto.request.CategoryRequest;
import com.sarthak.finance.dto.response.CategoryResponse;
import com.sarthak.finance.exception.BadRequestException;
import com.sarthak.finance.exception.DuplicateResourceException;
import com.sarthak.finance.exception.ResourceNotFoundException;
import com.sarthak.finance.model.Category;
import com.sarthak.finance.model.TransactionType;
import com.sarthak.finance.model.User;
import com.sarthak.finance.repository.CategoryRepository;
import com.sarthak.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    private static final Map<String, TransactionType> DEFAULT_CATEGORIES = Map.of(
            "Salary", TransactionType.INCOME,
            "Freelance", TransactionType.INCOME,
            "Investments", TransactionType.INCOME,
            "Other Income", TransactionType.INCOME,
            "Food", TransactionType.EXPENSE,
            "Rent", TransactionType.EXPENSE,
            "Utilities", TransactionType.EXPENSE,
            "Entertainment", TransactionType.EXPENSE,
            "Transportation", TransactionType.EXPENSE,
            "Other Expense", TransactionType.EXPENSE
    );

    public List<CategoryResponse> getAllCategoriesForUser(User user) {
        ensureDefaultCategoriesExist();
        return categoryRepository.findByUserOrUserIsNull(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse createCategory(CategoryRequest request, User user) {
        ensureDefaultCategoriesExist();
        String name = request.getName().trim();

        if (categoryRepository.findByNameAndUser(name, user).isPresent()) {
            throw new DuplicateResourceException(
                    "You already have a category named '" + name + "' of type " + request.getType());
        }
        if (categoryRepository.findByNameAndUserIsNull(name).isPresent()) {
            throw new DuplicateResourceException(
                    "A default category named '" + name + "' already exists");
        }

        Category category = Category.builder()
                .name(name)
                .type(request.getType())
                .isDefault(false)
                .user(user)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    public void deleteCategory(Long categoryId, User user) {
        deleteCategoryByIdOrName(String.valueOf(categoryId), user);
    }

    public void deleteCategoryByIdOrName(String idOrName, User user) {
        Category category = null;

        try {
            Long categoryId = Long.parseLong(idOrName);
            Category cat = categoryRepository.findById(categoryId).orElse(null);
            if (cat != null && (cat.getUser() == null || cat.getUser().getId().equals(user.getId()))) {
                category = cat;
            }
        } catch (NumberFormatException ignored) {
        }

        if (category == null) {
            String trimmed = idOrName.trim();
            category = categoryRepository.findByNameAndUser(trimmed, user)
                    .or(() -> categoryRepository.findByNameAndUserIsNull(trimmed))
                    .orElseGet(() -> categoryRepository.findByUserOrUserIsNull(user).stream()
                            .filter(c -> c.getName().equalsIgnoreCase(trimmed))
                            .findFirst()
                            .orElse(null));
        }

        if (category == null) {
            for (Map.Entry<String, TransactionType> entry : DEFAULT_CATEGORIES.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(idOrName.trim())) {
                    throw new BadRequestException("Default categories cannot be deleted");
                }
            }
            throw new ResourceNotFoundException("Category not found with identifier: " + idOrName);
        }

        if (category.isDefault() || category.getUser() == null) {
            throw new BadRequestException("Default categories cannot be deleted");
        }

        if (!category.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only delete your own custom categories");
        }

        final Category finalCategory = category;
        if (transactionRepository.findByUserOrderByDateDescCreatedAtDesc(user).stream()
                .anyMatch(t -> t.getCategory().getId().equals(finalCategory.getId()))) {
            throw new BadRequestException(
                    "Cannot delete category '" + finalCategory.getName() + "' because it has transactions linked to it");
        }

        categoryRepository.delete(finalCategory);
    }

    public Category findAccessibleCategory(Long categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return category;
    }

    public Category findAccessibleCategoryByName(String categoryName, User user) {
        String trimmed = categoryName.trim();
        Category found = categoryRepository.findByNameAndUser(trimmed, user)
                .or(() -> categoryRepository.findByNameAndUserIsNull(trimmed))
                .orElseGet(() -> categoryRepository.findByUserOrUserIsNull(user).stream()
                        .filter(c -> c.getName().equalsIgnoreCase(trimmed))
                        .findFirst()
                        .orElse(null));

        if (found != null) {
            return found;
        }

        for (Map.Entry<String, TransactionType> entry : DEFAULT_CATEGORIES.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(trimmed)) {
                Category newDefault = Category.builder()
                        .name(entry.getKey())
                        .type(entry.getValue())
                        .isDefault(true)
                        .user(null)
                        .build();
                return categoryRepository.save(newDefault);
            }
        }

        throw new ResourceNotFoundException("Category not found with name: " + categoryName);
    }

    public synchronized void ensureDefaultCategoriesExist() {
        for (Map.Entry<String, TransactionType> entry : DEFAULT_CATEGORIES.entrySet()) {
            try {
                if (categoryRepository.findByNameAndUserIsNull(entry.getKey()).isEmpty()) {
                    Category category = Category.builder()
                            .name(entry.getKey())
                            .type(entry.getValue())
                            .isDefault(true)
                            .user(null)
                            .build();
                    categoryRepository.save(category);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .defaultCategory(category.isDefault())
                .build();
    }
}
