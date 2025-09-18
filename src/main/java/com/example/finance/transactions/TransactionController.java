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

    @PostMapping("/deposit/{accountId}")
    public TransactionDto deposit(@RequestBody TransactionRequest request, @PathVariable Long accountId, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return transactionService.deposit(accountId, request, userId);
    }

    @PostMapping("/withdraw/{accountId}")
    public TransactionDto withdraw(@RequestBody TransactionRequest request, @PathVariable Long accountId, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return transactionService.withdraw(accountId, request, userId);
    }
    
    @PostMapping("/transfer/{sourceAccountId}/to/{destinationAccountId}")
    public TransactionDto transfer(@RequestBody TransactionRequest request, @PathVariable Long sourceAccountId, @PathVariable Long destinationAccountId, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return transactionService.transfer(sourceAccountId, destinationAccountId, request, userId);
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionDto> getTransactions(@PathVariable Long accountId, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return transactionService.getTransactions(accountId, userId);
    }
}
