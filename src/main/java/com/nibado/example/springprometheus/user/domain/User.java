package com.nibado.example.springprometheus.user.domain;

import lombok.Data;

@Data
public class User {
    private final String email;
    private final String password;
}
