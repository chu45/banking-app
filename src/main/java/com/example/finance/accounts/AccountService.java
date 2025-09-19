package com.example.finance.accounts;

import org.springframework.stereotype.Service;

import com.example.finance.users.User;
import com.example.finance.users.UserRepository;
import com.example.finance.exceptions.AccountNotFoundException;
import com.example.finance.exceptions.UnauthorizedAccountAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    public Page<AccountDto> getUserAccounts(Long userId, Pageable pageable) {
        return accountRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(accountMapper::toDto);
    }

    public AccountDto getAccountById(String accountNumber, Long userId) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new RuntimeException("Account not found"));
        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Account not found");
        }
        return accountMapper.toDto(account);
    }


        public void deleteAccount(String accountNumber) {
        accountRepository.deleteByAccountNumber(accountNumber);
    }

    // Method to validate if account belongs to user
    public void validateAccountOwnership(String accountNumber, Long userId) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccountAccessException("Access denied: You are not authorized to access this account");
        }
    }
    
    // Method to suspend an account
    public AccountDto suspendAccount(String accountNumber, Long userId) {
        validateAccountOwnership(accountNumber, userId);
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        account.setAccountStatus(Account.AccountStatus.SUSPENDED);
        return accountMapper.toDto(accountRepository.save(account));
    }
    
    // Method to activate an account
    public AccountDto activateAccount(String accountNumber, Long userId) {
        validateAccountOwnership(accountNumber, userId);
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        account.setAccountStatus(Account.AccountStatus.ACTIVE);
        return accountMapper.toDto(accountRepository.save(account));
    }
    
    // Method to get account status
    public Account.AccountStatus getAccountStatus(String accountNumber, Long userId) {
            validateAccountOwnership(accountNumber, userId);
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        return account.getAccountStatus();
    }
    
}
