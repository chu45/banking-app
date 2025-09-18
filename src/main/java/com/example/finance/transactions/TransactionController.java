package com.example.finance.transactions;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import com.example.finance.auth.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;

    public TransactionController(TransactionService transactionService, JwtUtil jwtUtil) {
        this.transactionService = transactionService;
        this.jwtUtil = jwtUtil;
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

    @GetMapping("/account/{accountId}")
    public List<TransactionDto> getTransactions(@PathVariable Long accountId, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return transactionService.getTransactions(accountId, userId);
    }
    
    @GetMapping("/ref/{transactionRef}")
    public TransactionDto getTransactionByRef(@PathVariable String transactionRef) {
        return transactionService.getTransactionByRef(transactionRef);
    }
}
