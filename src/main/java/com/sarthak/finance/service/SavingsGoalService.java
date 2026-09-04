package com.sarthak.finance.service;

import com.sarthak.finance.dto.request.SavingsGoalRequest;
import com.sarthak.finance.dto.request.UpdateSavingsGoalRequest;
import com.sarthak.finance.dto.response.SavingsGoalResponse;
import com.sarthak.finance.exception.BadRequestException;
import com.sarthak.finance.exception.ResourceNotFoundException;
import com.sarthak.finance.model.SavingsGoal;
import com.sarthak.finance.model.Transaction;
import com.sarthak.finance.model.TransactionType;
import com.sarthak.finance.model.User;
import com.sarthak.finance.repository.SavingsGoalRepository;
import com.sarthak.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;

    public SavingsGoalResponse createGoal(SavingsGoalRequest request, User user) {
        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        if (startDate.isAfter(request.getTargetDate())) {
            throw new BadRequestException("Start date cannot be after target date");
        }

        SavingsGoal goal = SavingsGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(startDate)
                .user(user)
                .build();

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return mapToResponse(saved, user);
    }

    public List<SavingsGoalResponse> getAllGoals(User user) {
        return savingsGoalRepository.findByUser(user)
                .stream()
                .map(goal -> mapToResponse(goal, user))
                .collect(Collectors.toList());
    }

    public SavingsGoalResponse getGoalById(Long id, User user) {
        SavingsGoal goal = findUserGoal(id, user);
        return mapToResponse(goal, user);
    }

    public SavingsGoalResponse updateGoal(Long id, UpdateSavingsGoalRequest request, User user) {
        SavingsGoal goal = findUserGoal(id, user);

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be in the future");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal updated = savingsGoalRepository.save(goal);
        return mapToResponse(updated, user);
    }

    public void deleteGoal(Long id, User user) {
        SavingsGoal goal = findUserGoal(id, user);
        savingsGoalRepository.delete(goal);
    }

    private SavingsGoal findUserGoal(Long id, User user) {
        return savingsGoalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + id));
    }

    private SavingsGoalResponse mapToResponse(SavingsGoal goal, User user) {
        List<Transaction> transactions = transactionRepository.findByUserOrderByDateDescCreatedAtDesc(user)
                .stream()
                .filter(t -> !t.getDate().isBefore(goal.getStartDate()))
                .collect(Collectors.toList());

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getCategory().getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getCategory().getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentSavings = totalIncome.subtract(totalExpenses);
        BigDecimal remaining = goal.getTargetAmount().subtract(currentSavings);

        double percentComplete = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentComplete = currentSavings
                    .multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        percentComplete = Math.max(0, Math.min(100, percentComplete));

        BigDecimal cleanProgress = cleanDecimal(currentSavings);
        BigDecimal cleanRemaining = cleanDecimal(remaining.max(BigDecimal.ZERO));

        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .currentSavings(cleanProgress)
                .currentProgress(cleanProgress)
                .percentComplete(percentComplete)
                .progressPercentage(percentComplete)
                .remainingAmount(cleanRemaining)
                .build();
    }

    private BigDecimal cleanDecimal(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
