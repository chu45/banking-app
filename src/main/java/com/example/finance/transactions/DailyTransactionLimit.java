package com.example.finance.transactions;

import com.example.finance.accounts.Account;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_transaction_limits", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"account_id", "transaction_type", "date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyTransactionLimit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "Account is required")
    private Account account;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    @NotNull(message = "Transaction type is required")
    private Transaction.TransactionType transactionType;
    
    @Column(name = "date", nullable = false)
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    @Column(name = "daily_limit", precision = 15, scale = 2, nullable = false)
    @NotNull(message = "Daily limit is required")
    private BigDecimal dailyLimit;
    
    @Column(name = "used_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal usedAmount = BigDecimal.ZERO;
    
    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount = 0;
    
    @Column(name = "max_transaction_count", nullable = false)
    private Integer maxTransactionCount = 100; // Default daily transaction count limit
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public BigDecimal getRemainingLimit() {
        return dailyLimit.subtract(usedAmount);
    }
    
    public Integer getRemainingTransactionCount() {
        return maxTransactionCount - transactionCount;
    }
    
    public boolean canAccommodateTransaction(BigDecimal amount) {
        return getRemainingLimit().compareTo(amount) >= 0 && getRemainingTransactionCount() > 0;
    }
    
    public void addTransaction(BigDecimal amount) {
        this.usedAmount = this.usedAmount.add(amount);
        this.transactionCount++;
    }
}
