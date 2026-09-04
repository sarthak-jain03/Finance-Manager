package com.sarthak.finance.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlyReportResponse {

    private int year;
    private List<MonthSummary> monthlyBreakdown;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthSummary {
        private int month;
        private int year;
        private java.util.Map<String, BigDecimal> incomeByCategory;
        private java.util.Map<String, BigDecimal> expenseByCategory;
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal savings;
    }
}
