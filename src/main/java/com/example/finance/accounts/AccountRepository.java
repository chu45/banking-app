package com.example.finance.accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    List<Account> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    Page<Account> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    void deleteByAccountNumber(String accountNumber);
}
