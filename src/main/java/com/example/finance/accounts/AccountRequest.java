package com.example.finance.accounts;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.finance.accounts.Account.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;
    
    @JsonProperty("balance")
    private BigDecimal initialBalance = BigDecimal.ZERO;

}
