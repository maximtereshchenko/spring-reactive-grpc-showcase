package com.github.xini1.apigateway.dto;

import com.github.xini1.orders.read.rpc.*;
import com.github.xini1.orders.write.rpc.*;

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

    public ActivateDeactivateItemDto toActivateDeactivateItemDto(String itemId) {
        return new ActivateDeactivateItemDto(id, userType, itemId);
    }

    public AddItemToCartRequest toAddItemToCartRequest(AddItemToCartDto addItemToCartDto) {
        return AddItemToCartRequest.newBuilder()
                .setUserId(id)
                .setUserType(userType)
                .setItemId(addItemToCartDto.getItemId())
                .setQuantity(addItemToCartDto.getQuantity())
                .build();
    }

    public ViewCartRequest toViewCartRequest() {
        return ViewCartRequest.newBuilder()
                .setUserId(id)
                .setUserType(userType)
                .build();
    }
}
