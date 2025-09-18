package com.example.finance.transactions;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransactionRequest {
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String description;
}
