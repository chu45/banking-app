package com.example.finance.accounts;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    
    @GetMapping("/paginated")
    public Page<AccountDto> getUserAccountsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return accountService.getUserAccounts(userId, pageable);
    }

    @GetMapping("/{accountNumber}")
    public AccountDto getAccountById(@PathVariable String accountNumber, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        return accountService.getAccountById(accountNumber, userId);
    }

    @DeleteMapping("/{accountNumber}")
    public void deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccount(accountNumber);
    }

    @PatchMapping("/{accountNumber}/suspend")
    public AccountDto suspendAccount(@PathVariable String accountNumber, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        return accountService.suspendAccount(accountNumber, userId);
    }

    @PatchMapping("/{accountNumber}/activate")
    public AccountDto activateAccount(@PathVariable String accountNumber, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        return accountService.activateAccount(accountNumber, userId);
    }

    @GetMapping("/{accountNumber}/status")
        public Account.AccountStatus getAccountStatus(@PathVariable String accountNumber, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        return accountService.getAccountStatus(accountNumber, userId);
    }

}
