package com.example.finance.transactions;

import com.example.finance.accounts.Account;
import com.example.finance.exceptions.DailyLimitExceededException;
import com.example.finance.exceptions.MaximumTransferLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class TransactionLimitService {
    
    private final DailyTransactionLimitRepository dailyTransactionLimitRepository;
    
    @Value("${finance.transaction.max-transfer-limit:1000000.00}")
    private BigDecimal maxTransferLimit;
    
    @Value("${finance.transaction.max-deposit-limit:500000.00}")
    private BigDecimal maxDepositLimit;
    
    @Value("${finance.transaction.max-withdraw-limit:100000.00}")
    private BigDecimal maxWithdrawLimit;
    
    @Value("${finance.transaction.default-daily-limit:50000.00}")
    private BigDecimal defaultDailyLimit;
    
    @Value("${finance.transaction.default-daily-transaction-count:50}")
    private Integer defaultDailyTransactionCount;
    
    public TransactionLimitService(DailyTransactionLimitRepository dailyTransactionLimitRepository) {
        this.dailyTransactionLimitRepository = dailyTransactionLimitRepository;
    }
    
    /**
     * Validates if a transaction can be performed based on system-wide maximum limits
     */
    public void validateMaximumTransactionLimits(Transaction.TransactionType transactionType, BigDecimal amount) {
        switch (transactionType) {
            case TRANSFER:
                if (amount.compareTo(maxTransferLimit) > 0) {
                    throw new MaximumTransferLimitExceededException(
                        "Transfer amount " + amount + " exceeds maximum allowed limit of " + maxTransferLimit
                    );
                }
                break;
            case DEPOSIT:
                if (amount.compareTo(maxDepositLimit) > 0) {
                    throw new MaximumTransferLimitExceededException(
                        "Deposit amount " + amount + " exceeds maximum allowed limit of " + maxDepositLimit
                    );
                }
                break;
            case WITHDRAW:
                if (amount.compareTo(maxWithdrawLimit) > 0) {
                    throw new MaximumTransferLimitExceededException(
                        "Withdrawal amount " + amount + " exceeds maximum allowed limit of " + maxWithdrawLimit
                    );
                }
                break;
        }
    }
    
    /**
     * Validates if a transaction can be performed based on daily limits
     */
    @Transactional
    public void validateDailyLimit(Account account, Transaction.TransactionType transactionType, BigDecimal amount) {
        LocalDate today = LocalDate.now();
        
        DailyTransactionLimit dailyLimit = getOrCreateDailyLimit(account, transactionType, today);
        
        if (!dailyLimit.canAccommodateTransaction(amount)) {
            if (dailyLimit.getRemainingLimit().compareTo(amount) < 0) {
                throw new DailyLimitExceededException(
                    "Transaction amount " + amount + " exceeds daily remaining limit of " + 
                    dailyLimit.getRemainingLimit() + " for " + transactionType + " transactions"
                );
            } else {
                throw new DailyLimitExceededException(
                    "Daily transaction count limit exceeded. Maximum " + dailyLimit.getMaxTransactionCount() + 
                    " transactions allowed per day for " + transactionType + " transactions"
                );
            }
        }
    }
    
    /**
     * Records a successful transaction against daily limits
     */
    @Transactional
    public void recordTransaction(Account account, Transaction.TransactionType transactionType, BigDecimal amount) {
        LocalDate today = LocalDate.now();
        
        DailyTransactionLimit dailyLimit = getOrCreateDailyLimit(account, transactionType, today);
        dailyLimit.addTransaction(amount);
        dailyTransactionLimitRepository.save(dailyLimit);
    }
    
    /**
     * Gets or creates a daily limit record for the given account, transaction type, and date
     */
    private DailyTransactionLimit getOrCreateDailyLimit(Account account, Transaction.TransactionType transactionType, LocalDate date) {
        Optional<DailyTransactionLimit> existingLimit = dailyTransactionLimitRepository
            .findByAccountAndTransactionTypeAndDate(account, transactionType, date);
        
        if (existingLimit.isPresent()) {
            return existingLimit.get();
        }
        
        // Create new daily limit record with default values
        DailyTransactionLimit newLimit = new DailyTransactionLimit();
        newLimit.setAccount(account);
        newLimit.setTransactionType(transactionType);
        newLimit.setDate(date);
        newLimit.setDailyLimit(getDefaultDailyLimitForType(transactionType));
        newLimit.setUsedAmount(BigDecimal.ZERO);
        newLimit.setTransactionCount(0);
        newLimit.setMaxTransactionCount(defaultDailyTransactionCount);
        
        return dailyTransactionLimitRepository.save(newLimit);
    }
    
    /**
     * Gets default daily limit based on transaction type
     */
    private BigDecimal getDefaultDailyLimitForType(Transaction.TransactionType transactionType) {
        switch (transactionType) {
            case TRANSFER:
                return defaultDailyLimit;
            case DEPOSIT:
                return defaultDailyLimit.multiply(BigDecimal.valueOf(2)); // Higher limit for deposits
            case WITHDRAW:
                return defaultDailyLimit.multiply(BigDecimal.valueOf(0.5)); // Lower limit for withdrawals
            default:
                return defaultDailyLimit;
        }
    }
    
    /**
     * Updates daily limit for a specific account and transaction type
     */
    @Transactional
    public void updateDailyLimit(Account account, Transaction.TransactionType transactionType, 
                                BigDecimal newLimit, Integer newTransactionCountLimit) {
        LocalDate today = LocalDate.now();
        
        DailyTransactionLimit dailyLimit = getOrCreateDailyLimit(account, transactionType, today);
        dailyLimit.setDailyLimit(newLimit);
        if (newTransactionCountLimit != null) {
            dailyLimit.setMaxTransactionCount(newTransactionCountLimit);
        }
        dailyTransactionLimitRepository.save(dailyLimit);
    }
    
    /**
     * Gets remaining daily limit for an account and transaction type
     */
    public BigDecimal getRemainingDailyLimit(Account account, Transaction.TransactionType transactionType) {
        LocalDate today = LocalDate.now();
        
          // Check if there's already a daily limit record for today
        Optional<DailyTransactionLimit> dailyLimit = dailyTransactionLimitRepository
            .findByAccountAndTransactionTypeAndDate(account, transactionType, today);
        
        if (dailyLimit.isPresent()) {
            return dailyLimit.get().getRemainingLimit();
        }
        
        return getDefaultDailyLimitForType(transactionType);
    }
}
