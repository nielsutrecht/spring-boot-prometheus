package com.nibado.example.springprometheus.user.domain;

import lombok.Data;

import java.util.UUID;

@Data
public class UserLoginResponse {
    private final UUID token;
}
