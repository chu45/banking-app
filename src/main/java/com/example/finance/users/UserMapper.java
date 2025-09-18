package com.example.finance.users;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(RegisterRequest request);
    void updateEntity(UserDto userDto, @MappingTarget User user);
}
