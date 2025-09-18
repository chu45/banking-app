package com.example.finance.transactions;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit/{accountId}")
    public TransactionDto deposit(@RequestBody TransactionRequest request, @PathVariable Long accountId) {
        return transactionService.deposit(accountId, request);
    }

    @PostMapping("/withdraw/{accountId}")
    public TransactionDto withdraw(@RequestBody TransactionRequest request, @PathVariable Long accountId) {
        return transactionService.withdraw(accountId, request);
    }
    
    @PostMapping("/transfer/{sourceAccountId}/to/{destinationAccountId}")
    public TransactionDto transfer(@RequestBody TransactionRequest request, @PathVariable Long sourceAccountId, @PathVariable Long destinationAccountId) {
        return transactionService.transfer(sourceAccountId, destinationAccountId, request);
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionDto> getTransactions(@PathVariable Long accountId) {
        return transactionService.getTransactions(accountId);
    }
}
