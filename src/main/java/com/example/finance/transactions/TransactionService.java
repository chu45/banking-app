package com.example.finance.transactions;

import org.springframework.stereotype.Service;

import com.example.finance.accounts.AccountRepository;
import com.example.finance.transactions.Transaction.TransactionType;
import com.example.finance.accounts.Account;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    
    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = transactionMapper;
    }

    @Transactional
    public TransactionDto deposit(Long accountId, TransactionRequest request) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("Account not found"));
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);
        Transaction transaction = new Transaction(null, null, account, TransactionType.DEPOSIT, request.getAmount(), request.getDescription(), null);
        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionDto withdraw(Long accountId, TransactionRequest request) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("Account not found"));
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);
        Transaction transaction = new Transaction(null, account, null, TransactionType.WITHDRAW, request.getAmount(), request.getDescription(), null);
        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionDto transfer(Long sourceAccountId, Long destinationAccountId, TransactionRequest request) {
        Account sourceAccount = accountRepository.findById(sourceAccountId).orElseThrow(() -> new RuntimeException("Source account not found"));
        Account destinationAccount = accountRepository.findById(destinationAccountId).orElseThrow(() -> new RuntimeException("Destination account not found"));
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
        Transaction transaction = new Transaction(null, sourceAccount, destinationAccount, TransactionType.TRANSFER, request.getAmount(), request.getDescription(), null);
        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    public List<TransactionDto> getTransactions(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("Account not found"));
        return transactionRepository.findBySourceAccountOrDestinationAccount(account, account).stream().map(transactionMapper::toDto).collect(Collectors.toList());
    }

}
