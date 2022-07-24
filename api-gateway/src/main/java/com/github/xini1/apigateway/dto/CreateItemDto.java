package com.github.xini1.apigateway.dto;

import com.github.xini1.orders.write.rpc.*;

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

    public CreateItemRequest toCreateItemRequest() {
        return CreateItemRequest.newBuilder()
                .setUserId(userId)
                .setUserType(userType)
                .setName(name)
                .build();
    }
}
