package com.example.finance.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import com.example.finance.accounts.Account;
    
public interface TransactionRepository extends JpaRepository<Transaction, Long>     {
    List<Transaction> findBySourceAccountOrDestinationAccount(Account sourceAccount, Account destinationAccount);
    Page<Transaction> findBySourceAccountOrDestinationAccount(Account sourceAccount, Account destinationAccount, Pageable pageable);
    Optional<Transaction> findByTransactionRef(String transactionRef);
}
