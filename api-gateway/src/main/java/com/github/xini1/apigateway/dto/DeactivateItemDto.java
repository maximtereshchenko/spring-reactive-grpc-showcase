package com.github.xini1.apigateway.dto;

/**
 * @author Maxim Tereshchenko
 */
public final class DeactivateItemDto {

    private final String userId;
    private final String userType;
    private final String itemId;

    public DeactivateItemDto(String userId, String userType, String itemId) {
        this.userId = userId;
        this.userType = userType;
        this.itemId = itemId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserType() {
        return userType;
    }

    public String getItemId() {
        return itemId;
    }
}
