package com.github.xini1.apigateway.dto;

/**
 * @author Maxim Tereshchenko
 */
public final class UserDto {

    private final String id;
    private final String userType;

    public UserDto(String id, String userType) {
        this.id = id;
        this.userType = userType;
    }

    public String getId() {
        return id;
    }

    public String getUserType() {
        return userType;
    }

    public CreateItemDto toCreateItemDto(String itemName) {
        return new CreateItemDto(id, userType, itemName);
    }

    public DeactivateItemDto toDeactivateItemDto(String itemId) {
        return new DeactivateItemDto(id, userType, itemId);
    }
}
