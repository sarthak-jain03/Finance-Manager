package com.sarthak.finance.service;

import com.sarthak.finance.dto.response.MonthlyReportResponse;
import com.sarthak.finance.dto.response.YearlyReportResponse;
import com.sarthak.finance.exception.BadRequestException;
import com.sarthak.finance.model.TransactionType;
import com.sarthak.finance.model.User;
import com.sarthak.finance.model.Transaction;
import com.sarthak.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    public MonthlyReportResponse getMonthlyReport(int month, int year, User user) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserOrderByDateDescCreatedAtDesc(user)
                .stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .collect(Collectors.toList());

        Map<String, BigDecimal> incomeByCategory = buildCategoryBreakdown(transactions, TransactionType.INCOME);
        Map<String, BigDecimal> expenseByCategory = buildCategoryBreakdown(transactions, TransactionType.EXPENSE);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getCategory().getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getCategory().getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .incomeByCategory(incomeByCategory)
                .expenseByCategory(expenseByCategory)
                .totalIncome(cleanDecimal(totalIncome))
                .totalExpenses(cleanDecimal(totalExpenses))
                .netSavings(cleanDecimal(netSavings))
                .build();
    }

    public YearlyReportResponse getYearlyReport(int year, User user) {
        List<YearlyReportResponse.MonthSummary> monthlyBreakdown = new ArrayList<>();
        BigDecimal yearlyIncome = BigDecimal.ZERO;
        BigDecimal yearlyExpenses = BigDecimal.ZERO;

        for (int month = 1; month <= 12; month++) {
            MonthlyReportResponse monthReport = getMonthlyReport(month, year, user);
            monthlyBreakdown.add(YearlyReportResponse.MonthSummary.builder()
                    .month(month)
                    .year(year)
                    .incomeByCategory(monthReport.getIncomeByCategory())
                    .expenseByCategory(monthReport.getExpenseByCategory())
                    .totalIncome(monthReport.getTotalIncome())
                    .totalExpenses(monthReport.getTotalExpenses())
                    .savings(monthReport.getNetSavings())
                    .build());
            yearlyIncome = yearlyIncome.add(monthReport.getTotalIncome());
            yearlyExpenses = yearlyExpenses.add(monthReport.getTotalExpenses());
        }

        return YearlyReportResponse.builder()
                .year(year)
                .monthlyBreakdown(monthlyBreakdown)
                .totalIncome(cleanDecimal(yearlyIncome))
                .totalExpenses(cleanDecimal(yearlyExpenses))
                .netSavings(cleanDecimal(yearlyIncome.subtract(yearlyExpenses)))
                .build();
    }

    private Map<String, BigDecimal> buildCategoryBreakdown(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getCategory().getType() == type)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }

    private BigDecimal cleanDecimal(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
