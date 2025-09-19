package com.example.finance.transactions;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import com.example.finance.auth.JwtUtil;
import com.example.finance.accounts.AccountRepository;
import com.example.finance.accounts.Account;
import com.example.finance.exceptions.AccountNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import com.example.finance.accounts.AccountService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionLimitService transactionLimitService;
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;
    private final AccountService accountService;
    public TransactionController(TransactionService transactionService, TransactionLimitService transactionLimitService,
                               AccountRepository accountRepository, JwtUtil jwtUtil, AccountService accountService) {
        this.transactionService = transactionService;
        this.transactionLimitService = transactionLimitService;
        this.accountRepository = accountRepository;
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
    }

    // Helper method to extract userId from JWT
    private Long getUserIdFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        try {
            String token = header.substring(7); // remove "Bearer "
            return Long.parseLong(jwtUtil.extractUserId(token));
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token");
        }
    }

    @PostMapping("/deposit/{accountNumber}")
    public TransactionDto deposit(@RequestBody TransactionRequest request, @PathVariable String accountNumber, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return transactionService.deposit(accountNumber, request, userId);
    }

    @PostMapping("/withdraw/{accountNumber}")
    public TransactionDto withdraw(@RequestBody TransactionRequest request, @PathVariable String accountNumber, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return transactionService.withdraw(accountNumber, request, userId);
    }
    
    @PostMapping("/transfer/{sourceAccountNumber}/to/{destinationAccountNumber}")
    public TransactionDto transfer(@RequestBody TransactionRequest request, @PathVariable String sourceAccountNumber, @PathVariable String destinationAccountNumber, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return transactionService.transfer(sourceAccountNumber, destinationAccountNumber, request, userId);
    }

    @GetMapping("/account/{accountNumber}")
    public List<TransactionDto> getTransactions(@PathVariable String accountNumber, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return transactionService.getTransactions(accountNumber, userId);
    }
    
    @GetMapping("/account/{accountNumber}/paginated")
    public Page<TransactionDto> getTransactionsPaginated(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return transactionService.getTransactionsPaginated(accountNumber, userId, pageable);
    }
    
    @GetMapping("/ref/{transactionRef}")
    public TransactionDto getTransactionByRef(@PathVariable String transactionRef) {
        return transactionService.getTransactionByRef(transactionRef);
    }
    
    @GetMapping("/limits/{accountNumber}")
    public Map<String, Object> getDailyLimits(@PathVariable String accountNumber, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        
        // Validate account ownership
        accountService.validateAccountOwnership(account.getAccountNumber(), userId);
        Map<String, Object> limits = Map.of(
            "DEPOSIT", Map.of(
                "remaining", transactionLimitService.getRemainingDailyLimit(account, Transaction.TransactionType.DEPOSIT)
            ),
            "WITHDRAW", Map.of(
                "remaining", transactionLimitService.getRemainingDailyLimit(account, Transaction.TransactionType.WITHDRAW)
            ),
            "TRANSFER", Map.of(
                "remaining", transactionLimitService.getRemainingDailyLimit(account, Transaction.TransactionType.TRANSFER)
            )
        );
        
        return limits;
    }
    
    @PutMapping("/limits/{accountNumber}")
    public Map<String, String> updateDailyLimit(
            @PathVariable String accountNumber,
            @RequestParam String transactionType,
            @RequestParam BigDecimal newLimit,
            @RequestParam(required = false) Integer newTransactionCountLimit,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromToken(httpRequest);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        
        Transaction.TransactionType type = Transaction.TransactionType.valueOf(transactionType.toUpperCase());
        transactionLimitService.updateDailyLimit(account, type, newLimit, newTransactionCountLimit);
        
        return Map.of("message", "Daily limit updated successfully");
    }
}
