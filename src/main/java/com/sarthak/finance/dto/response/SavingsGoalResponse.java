package com.sarthak.finance.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoalResponse {

    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private LocalDate startDate;
    private BigDecimal currentSavings;
    private BigDecimal currentProgress;
    private double percentComplete;
    private double progressPercentage;
    private BigDecimal remainingAmount;
}
