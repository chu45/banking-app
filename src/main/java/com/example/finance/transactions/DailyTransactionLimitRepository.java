package com.example.finance.transactions;

import com.example.finance.accounts.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyTransactionLimitRepository extends JpaRepository<DailyTransactionLimit, Long> {
    
    Optional<DailyTransactionLimit> findByAccountAndTransactionTypeAndDate(
        Account account, 
        Transaction.TransactionType transactionType, 
        LocalDate date
    );
}
