package com.github.xini1.apigateway.dto;

import com.github.xini1.users.rpc.LoginRequest;

/**
 * @author Maxim Tereshchenko
 */
public final class LoginDto {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LoginRequest toLoginRequest() {
        return LoginRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();
    }
}
