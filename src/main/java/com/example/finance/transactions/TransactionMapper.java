package com.example.finance.transactions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction toEntity(TransactionRequest request);

    @Mapping(source = "sourceAccount.id", target = "sourceAccountId")
    @Mapping(source = "destinationAccount.id", target = "destinationAccountId")
    TransactionDto toDto(Transaction transaction);
    void updateEntity(TransactionRequest request, @MappingTarget Transaction transaction);

}
