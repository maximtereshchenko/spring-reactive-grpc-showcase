package com.github.xini1.apigateway.dto;

import com.github.xini1.orders.write.rpc.*;

/**
 * @author Maxim Tereshchenko
 */
public final class ActivateDeactivateItemDto {

    private final String userId;
    private final String userType;
    private final String itemId;

    public ActivateDeactivateItemDto(String userId, String userType, String itemId) {
        this.userId = userId;
        this.userType = userType;
        this.itemId = itemId;
    }

    public DeactivateItemRequest toDeactivateItemRequest() {
        return DeactivateItemRequest.newBuilder()
                .setUserId(userId)
                .setUserType(userType)
                .setItemId(itemId)
                .build();
    }

    public ActivateItemRequest toActivateItemRequest() {
        return ActivateItemRequest.newBuilder()
                .setUserId(userId)
                .setUserType(userType)
                .setItemId(itemId)
                .build();
    }
}
