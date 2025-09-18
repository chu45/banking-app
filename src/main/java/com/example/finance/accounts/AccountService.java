package com.example.finance.accounts;

import org.springframework.stereotype.Service;

import com.example.finance.accounts.AccountDto;
import com.example.finance.accounts.AccountRequest;
import com.example.finance.users.User;
import com.example.finance.accounts.AccountRepository;
import com.example.finance.users.UserRepository;
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
    
}
