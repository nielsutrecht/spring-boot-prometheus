package com.nibado.example.springprometheus.user.domain;

import lombok.Data;

@Data
public class UserLogin {
    private String email;
    private String password;
}
