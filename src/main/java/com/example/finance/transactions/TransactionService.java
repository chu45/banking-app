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
import java.util.UUID;
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
    
    private String generateTransactionRef() {
        // Generate a unique transaction reference ID Format: TXN-{UUID first 8 chars}-{timestamp last 6 digits}
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        return "TXN-" + uuid + "-" + timestamp;
    }

    @Transactional(rollbackFor = Exception.class)
    public TransactionDto deposit(String accountNumber, TransactionRequest request, Long userId) {
        // Find account by account number
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        
        // Validate account ownership
        accountService.validateAccountOwnership(account.getId(), userId);
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException("Deposit amount must be positive");
        }
        validateAccountStatus(account);
        
        // Create transaction with PENDING status first
        String transactionRef = request.getTransactionRef() != null ? request.getTransactionRef() : generateTransactionRef();
        Transaction transaction = new Transaction(null, null, account, TransactionType.DEPOSIT, request.getAmount(), request.getDescription(), TransactionStatus.PENDING, transactionRef, null);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        try {
            // Process the deposit
            account.setBalance(account.getBalance().add(request.getAmount()));
            accountRepository.save(account);
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
    public TransactionDto withdraw(String accountNumber, TransactionRequest request, Long userId) {
        // Find account by account number
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        
        // Validate account ownership
        accountService.validateAccountOwnership(account.getId(), userId);
        
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException("Withdrawal amount must be positive");
        }
        validateAccountStatus(account);
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal. Available: " + account.getBalance() + ", Requested: " + request.getAmount());
        }
        
        // Create transaction with PENDING status first
        String transactionRef = request.getTransactionRef() != null ? request.getTransactionRef() : generateTransactionRef();
        Transaction transaction = new Transaction(null, account, null, TransactionType.WITHDRAW, request.getAmount(), request.getDescription(), TransactionStatus.PENDING, transactionRef, null);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        try {
            // Process the withdrawal
            account.setBalance(account.getBalance().subtract(request.getAmount()));
            accountRepository.save(account);
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
    public TransactionDto transfer(String sourceAccountNumber, String destinationAccountNumber, TransactionRequest request, Long userId) {
        // Find accounts by account number
        Account sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Source account with number " + sourceAccountNumber + " not found"));
        Account destinationAccount = accountRepository.findByAccountNumber(destinationAccountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Destination account with number " + destinationAccountNumber + " not found"));
        
        accountService.validateAccountOwnership(sourceAccount.getId(), userId);
        
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException("Transfer amount must be positive");
        }
        
        validateAccountStatus(sourceAccount);
        validateAccountStatus(destinationAccount);
        
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for transfer. Available: " + sourceAccount.getBalance() + ", Requested: " + request.getAmount());
        }
        
        // Create transaction with PENDING status first
        String transactionRef = request.getTransactionRef() != null ? request.getTransactionRef() : generateTransactionRef();
        Transaction transaction = new Transaction(null, sourceAccount, destinationAccount, 
            TransactionType.TRANSFER, request.getAmount(), request.getDescription(), TransactionStatus.PENDING, transactionRef, null);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        try {
            // Perform atomic balance updates
            BigDecimal sourceNewBalance = sourceAccount.getBalance().subtract(request.getAmount());
            BigDecimal destinationNewBalance = destinationAccount.getBalance().add(request.getAmount());
            
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
    
    public TransactionDto getTransactionByRef(String transactionRef) {
        Transaction transaction = transactionRepository.findByTransactionRef(transactionRef)
            .orElseThrow(() -> new RuntimeException("Transaction with reference " + transactionRef + " not found"));
        return transactionMapper.toDto(transaction);
    }

}
