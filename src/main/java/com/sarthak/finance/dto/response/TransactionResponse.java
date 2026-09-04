package com.sarthak.finance.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sarthak.finance.model.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String category;
    private String description;
    private TransactionType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
