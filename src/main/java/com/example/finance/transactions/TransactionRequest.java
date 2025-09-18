package com.example.finance.transactions;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransactionRequest {
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String description;
    private String transactionRef;
}
