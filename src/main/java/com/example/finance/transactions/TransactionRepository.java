package com.example.finance.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import com.example.finance.accounts.Account;
    
public interface TransactionRepository extends JpaRepository<Transaction, Long>     {
    List<Transaction> findBySourceAccountOrDestinationAccount(Account sourceAccount, Account destinationAccount);
    Optional<Transaction> findByTransactionRef(String transactionRef);
}
