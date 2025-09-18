package com.example.finance.transactions;

import org.springframework.stereotype.Service;

import com.example.finance.accounts.AccountRepository;
import com.example.finance.accounts.AccountService;
import com.example.finance.transactions.Transaction.TransactionType;
import com.example.finance.transactions.Transaction.TransactionStatus;
import com.example.finance.accounts.Account;
import com.example.finance.exceptions.AccountNotFoundException;
import com.example.finance.exceptions.AccountSuspendedException;
import com.example.finance.exceptions.InsufficientBalanceException;
import com.example.finance.exceptions.InvalidTransactionAmountException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final AccountService accountService;
    
    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository, TransactionMapper transactionMapper, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = transactionMapper;
        this.accountService = accountService;
    }
    
    private void validateAccountStatus(Account account) {
        if (account.getAccountStatus() != Account.AccountStatus.ACTIVE) {
            throw new AccountSuspendedException("Account " + account.getAccountNumber() + " is suspended and cannot perform transactions");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public TransactionDto deposit(Long accountId, TransactionRequest request, Long userId) {
        // Validate account ownership
        accountService.validateAccountOwnership(accountId, userId);
        
        // Validate deposit amount is positive
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException("Deposit amount must be positive");
        }
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found"));
        
        // Validate account status
        validateAccountStatus(account);
        
        // Create transaction with PENDING status first
        Transaction transaction = new Transaction(null, null, account, TransactionType.DEPOSIT, request.getAmount(), request.getDescription(), TransactionStatus.PENDING, null);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        try {
            // Process the deposit
            account.setBalance(account.getBalance().add(request.getAmount()));
            accountRepository.save(account);
            
            // Update transaction status to COMPLETED
            savedTransaction.setStatus(TransactionStatus.COMPLETED);
            savedTransaction = transactionRepository.save(savedTransaction);
            
            return transactionMapper.toDto(savedTransaction);
        } catch (Exception e) {
            savedTransaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(savedTransaction);
            throw e; // Re-throw the exception
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public TransactionDto withdraw(Long accountId, TransactionRequest request, Long userId) {
        // Validate account ownership
        accountService.validateAccountOwnership(accountId, userId);
        
        // Validate withdrawal amount is positive
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException("Withdrawal amount must be positive");
        }
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found"));
        
        // Validate account status
        validateAccountStatus(account);
        
        // Validate sufficient balance before withdrawal
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal. Available: " + account.getBalance() + ", Requested: " + request.getAmount());
        }
        
        // Create transaction with PENDING status first
        Transaction transaction = new Transaction(null, account, null, TransactionType.WITHDRAW, request.getAmount(), request.getDescription(), TransactionStatus.PENDING, null);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        try {
            // Process the withdrawal
            account.setBalance(account.getBalance().subtract(request.getAmount()));
            accountRepository.save(account);
            
            // Update transaction status to COMPLETED
            savedTransaction.setStatus(TransactionStatus.COMPLETED);
            savedTransaction = transactionRepository.save(savedTransaction);
            
            return transactionMapper.toDto(savedTransaction);
        } catch (Exception e) {
            savedTransaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(savedTransaction);
            throw e; // Re-throw the exception
        }
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED,
        rollbackFor = Exception.class
    )
    public TransactionDto transfer(Long sourceAccountId, Long destinationAccountId, TransactionRequest request, Long userId) {
        // Validate source account ownership - user can only transfer from their own accounts
        accountService.validateAccountOwnership(sourceAccountId, userId);
        
        // Validate transfer amount is positive
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException("Transfer amount must be positive");
        }
        
        // Fetch accounts with pessimistic locking to prevent concurrent modifications
        Account sourceAccount = accountRepository.findById(sourceAccountId)
            .orElseThrow(() -> new AccountNotFoundException("Source account with ID " + sourceAccountId + " not found"));
        Account destinationAccount = accountRepository.findById(destinationAccountId)
            .orElseThrow(() -> new AccountNotFoundException("Destination account with ID " + destinationAccountId + " not found"));
        
        // Validate account statuses
        validateAccountStatus(sourceAccount);
        validateAccountStatus(destinationAccount);
        
        // Validate sufficient balance before making any changes
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for transfer. Available: " + sourceAccount.getBalance() + ", Requested: " + request.getAmount());
        }
        
        // Create transaction with PENDING status first
        Transaction transaction = new Transaction(null, sourceAccount, destinationAccount, 
            TransactionType.TRANSFER, request.getAmount(), request.getDescription(), TransactionStatus.PENDING, null);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        try {
            // Perform atomic balance updates
            BigDecimal sourceNewBalance = sourceAccount.getBalance().subtract(request.getAmount());
            BigDecimal destinationNewBalance = destinationAccount.getBalance().add(request.getAmount());
            
            // Update balances
            sourceAccount.setBalance(sourceNewBalance);
            destinationAccount.setBalance(destinationNewBalance);
            
            // Save both accounts - if either fails, transaction will rollback
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);
            
            // Update transaction status to COMPLETED
            savedTransaction.setStatus(TransactionStatus.COMPLETED);
            savedTransaction = transactionRepository.save(savedTransaction);
            
            return transactionMapper.toDto(savedTransaction);
        } catch (Exception e) {
            savedTransaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(savedTransaction);
            throw e; // Re-throw the exception
        }
    }

    public List<TransactionDto> getTransactions(Long accountId, Long userId) {
        // Validate account ownership
        accountService.validateAccountOwnership(accountId, userId);
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found"));
        return transactionRepository.findBySourceAccountOrDestinationAccount(account, account).stream().map(transactionMapper::toDto).collect(Collectors.toList());
    }

}
