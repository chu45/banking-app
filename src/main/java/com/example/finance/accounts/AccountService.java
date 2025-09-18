package com.example.finance.accounts;

import org.springframework.stereotype.Service;

import com.example.finance.users.User;
import com.example.finance.users.UserRepository;
import com.example.finance.exceptions.AccountNotFoundException;
import com.example.finance.exceptions.UnauthorizedAccountAccessException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountMapper = accountMapper;
    }
    
    public AccountDto createAccount(Long userId, AccountRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Account account = new Account();
        account.setUser(user);
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance());
        account.setAccountNumber("ACC" + userId + System.currentTimeMillis());
        return accountMapper.toDto(accountRepository.save(account));
    }   

    public List<AccountDto> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream()
            .map(accountMapper::toDto)
            .collect(Collectors.toList());
    }

    public AccountDto getAccountById(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("Account not found"));
        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Account not found");
        }
        return accountMapper.toDto(account);
    }


    public void deleteAccount(Long accountId) {
        accountRepository.deleteById(accountId);
    }

    // Method to validate if account belongs to user
    public void validateAccountOwnership(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found"));
        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccountAccessException("Access denied: You are not authorized to access this account");
        }
    }
    
    // Method to suspend an account
    public AccountDto suspendAccount(Long accountId, Long userId) {
        validateAccountOwnership(accountId, userId);
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found"));
        account.setAccountStatus(Account.AccountStatus.SUSPENDED);
        return accountMapper.toDto(accountRepository.save(account));
    }
    
    // Method to activate an account
    public AccountDto activateAccount(Long accountId, Long userId) {
        validateAccountOwnership(accountId, userId);
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found"));
        account.setAccountStatus(Account.AccountStatus.ACTIVE);
        return accountMapper.toDto(accountRepository.save(account));
    }
    
    // Method to get account status
    public Account.AccountStatus getAccountStatus(Long accountId, Long userId) {
        validateAccountOwnership(accountId, userId);
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found"));
        return account.getAccountStatus();
    }
    
}
