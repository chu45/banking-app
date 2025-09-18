package com.example.finance.accounts;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.finance.accounts.Account.AccountType;
import com.example.finance.accounts.Account.AccountStatus;

@Data
public class AccountDto {

    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private Long userId;
}
