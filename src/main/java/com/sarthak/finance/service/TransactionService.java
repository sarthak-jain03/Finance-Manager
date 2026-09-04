package com.sarthak.finance.service;

import com.sarthak.finance.dto.request.TransactionRequest;
import com.sarthak.finance.dto.request.UpdateTransactionRequest;
import com.sarthak.finance.dto.response.TransactionResponse;
import com.sarthak.finance.exception.BadRequestException;
import com.sarthak.finance.exception.ResourceNotFoundException;
import com.sarthak.finance.model.Category;
import com.sarthak.finance.model.Transaction;
import com.sarthak.finance.model.TransactionType;
import com.sarthak.finance.model.User;
import com.sarthak.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        validateTransactionDate(request.getDate());

        Category category = resolveCategory(request.getCategoryId(), request.getCategory(), user);

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .category(category)
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    public List<TransactionResponse> getTransactions(User user, LocalDate startDate, LocalDate endDate,
                                                     Long categoryId, TransactionType type) {
        return getTransactions(user, startDate, endDate, categoryId, null, type);
    }

    public List<TransactionResponse> getTransactions(User user, LocalDate startDate, LocalDate endDate,
                                                     Long categoryId, String categoryName, TransactionType type) {
        List<Transaction> transactions = transactionRepository.findByUserOrderByDateDescCreatedAtDesc(user);

        Long effectiveCategoryId = categoryId;
        String effectiveCategoryName = categoryName;

        if (effectiveCategoryId == null && categoryName != null && !categoryName.trim().isEmpty()) {
            try {
                effectiveCategoryId = Long.parseLong(categoryName.trim());
                effectiveCategoryName = null;
            } catch (NumberFormatException ignored) {
            }
        }

        final Long filterCategoryId = effectiveCategoryId;
        final String filterCategoryName = (filterCategoryId != null) ? null : (effectiveCategoryName != null ? effectiveCategoryName.trim() : null);

        return transactions.stream()
                .filter(t -> startDate == null || !t.getDate().isBefore(startDate))
                .filter(t -> endDate == null || !t.getDate().isAfter(endDate))
                .filter(t -> filterCategoryId == null || t.getCategory().getId().equals(filterCategoryId))
                .filter(t -> filterCategoryName == null || filterCategoryName.isEmpty() || t.getCategory().getName().equalsIgnoreCase(filterCategoryName))
                .filter(t -> type == null || t.getCategory().getType() == type)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(Long id, User user) {
        Transaction transaction = findUserTransaction(id, user);
        return mapToResponse(transaction);
    }

    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request, User user) {
        Transaction transaction = findUserTransaction(id, user);

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }

        if (request.getCategoryId() != null || (request.getCategory() != null && !request.getCategory().trim().isEmpty())) {
            Category category = resolveCategory(request.getCategoryId(), request.getCategory(), user);
            transaction.setCategory(category);
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    public void deleteTransaction(Long id, User user) {
        Transaction transaction = findUserTransaction(id, user);
        transactionRepository.delete(transaction);
    }

    private Category resolveCategory(Long categoryId, String categoryName, User user) {
        if (categoryId != null) {
            return categoryService.findAccessibleCategory(categoryId, user);
        } else if (categoryName != null && !categoryName.trim().isEmpty()) {
            return categoryService.findAccessibleCategoryByName(categoryName.trim(), user);
        } else {
            throw new BadRequestException("Category is required");
        }
    }

    private Transaction findUserTransaction(Long id, User user) {
        return transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    private void validateTransactionDate(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        Category cat = transaction.getCategory();

        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .category(cat.getName())
                .description(transaction.getDescription())
                .type(cat.getType())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
