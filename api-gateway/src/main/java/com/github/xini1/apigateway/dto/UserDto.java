package com.github.xini1.apigateway.dto;

import com.github.xini1.orders.read.rpc.ViewCartRequest;
import com.github.xini1.orders.read.rpc.ViewOrderedItemsRequest;
import com.github.xini1.orders.read.rpc.ViewTopOrderedItemsRequest;
import com.github.xini1.orders.write.rpc.AddItemToCartRequest;
import com.github.xini1.orders.write.rpc.OrderItemsInCartRequest;
import com.github.xini1.orders.write.rpc.RemoveItemFromCartRequest;

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

    public CreateItemDto toCreateItemDto(String itemName) {
        return new CreateItemDto(id, userType, itemName);
    }

    public ActivateDeactivateItemDto toActivateDeactivateItemDto(String itemId) {
        return new ActivateDeactivateItemDto(id, userType, itemId);
    }

    public AddItemToCartRequest toAddItemToCartRequest(AddRemoveItemToCartDto dto) {
        return AddItemToCartRequest.newBuilder()
                .setUserId(id)
                .setUserType(userType)
                .setItemId(dto.getItemId())
                .setQuantity(dto.getQuantity())
                .build();
    }

    public ViewCartRequest toViewCartRequest() {
        return ViewCartRequest.newBuilder()
                .setUserId(id)
                .setUserType(userType)
                .build();
    }

    public RemoveItemFromCartRequest toRemoveItemFromCartRequest(AddRemoveItemToCartDto dto) {
        return RemoveItemFromCartRequest.newBuilder()
                .setUserId(id)
                .setUserType(userType)
                .setItemId(dto.getItemId())
                .setQuantity(dto.getQuantity())
                .build();
    }

    public OrderItemsInCartRequest toOrderItemsInCartRequest() {
        return OrderItemsInCartRequest.newBuilder()
                .setUserId(id)
                .setUserType(userType)
                .build();
    }

    public ViewOrderedItemsRequest toViewOrderedItemsRequest() {
        return ViewOrderedItemsRequest.newBuilder()
                .setUserId(id)
                .setUserType(userType)
                .build();
    }

    public ViewTopOrderedItemsRequest toViewTopOrderedItemsRequest() {
        return ViewTopOrderedItemsRequest.newBuilder()
                .setUserType(userType)
                .build();
    }
}
