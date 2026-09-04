package com.sarthak.finance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTransactionRequest {

    @DecimalMin(value = "0.01", message = "Amount must be a positive value")
    private BigDecimal amount;

    private Long categoryId;

    private String category;

    private String description;

    private LocalDate date;

    public UpdateTransactionRequest(BigDecimal amount, Long categoryId, String description) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description;
    }
}
