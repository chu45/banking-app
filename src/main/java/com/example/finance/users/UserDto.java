package com.example.finance.users;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
}
