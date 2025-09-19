package com.example.finance.transactions;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LimitUpdateRequest {
    
    @NotBlank(message = "Transaction type is required")
    private String transactionType;
    
    @NotNull(message = "New limit is required")
    @Positive(message = "New limit must be positive")
    private BigDecimal newLimit;
    
    @Positive(message = "Transaction count limit must be positive")
    private Integer newTransactionCountLimit;
}
