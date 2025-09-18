package com.example.finance.accounts;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.finance.accounts.Account.AccountType;

@Data
public class AccountDto {

    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private Long userId;
}
