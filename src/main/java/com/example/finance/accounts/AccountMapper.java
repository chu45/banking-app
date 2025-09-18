package com.example.finance.accounts;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(source = "user.id", target = "userId")
    AccountDto toDto(Account account);
    
    Account toEntity(AccountRequest request);
    void updateEntity(AccountDto accountDto, @MappingTarget Account account);
}
