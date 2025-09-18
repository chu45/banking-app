package com.example.finance.accounts;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import com.example.finance.auth.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    public AccountController(AccountService accountService, JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
    }

      // Helper method to extract userId from JWT
      private Long getUserIdFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = header.substring(7); // remove "Bearer "
        return Long.parseLong(jwtUtil.extractUserId(token)); // ✅ now using your Option 2 util
    }

    @PostMapping
    public AccountDto createAccount(@RequestBody AccountRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return accountService.createAccount(userId, request);
    }
    
    @GetMapping
    public List<AccountDto> getUserAccounts(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        return accountService.getUserAccounts(userId);
    }

    @GetMapping("/{accountId}")
    public AccountDto getAccountById(@PathVariable Long accountId, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return accountService.getAccountById(accountId, userId);
    }

    @DeleteMapping("/{accountId}")
    public void deleteAccount(@PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
    }

}
