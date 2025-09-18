package com.example.finance.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.finance.transactions.Transaction.TransactionType;
import com.example.finance.transactions.Transaction.TransactionStatus;
import lombok.Data;

@Data
public class TransactionDto {
   private Long id;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
    
