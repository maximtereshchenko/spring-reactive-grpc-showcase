package com.github.xini1.apigateway.dto;

/**
 * @author Maxim Tereshchenko
 */
public final class CreateItemDto {

    private final String userId;
    private final String userType;
    private final String name;

    public CreateItemDto(String userId, String userType, String name) {
        this.userId = userId;
        this.userType = userType;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserType() {
        return userType;
    }

    public String getName() {
        return name;
    }
}
