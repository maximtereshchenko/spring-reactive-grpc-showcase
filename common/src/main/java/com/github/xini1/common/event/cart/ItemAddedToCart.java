package com.github.xini1.common.event.cart;

import com.github.xini1.common.event.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemAddedToCart implements CartEvent {

    private final UUID userId;
    private final UUID itemId;
    private final int quantity;
    private final long version;

    public ItemAddedToCart(UUID userId, UUID itemId, int quantity, long version) {
        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.version = version;
    }

    public ItemAddedToCart(Map<String, String> properties) {
        this(
                UUID.fromString(properties.get("userId")),
                UUID.fromString(properties.get("itemId")),
                Integer.parseInt(properties.get("quantity")),
                Long.parseLong(properties.get("version"))
        );
    }


    public UUID itemId() {
        return itemId;
    }

    public int quantity() {
        return quantity;
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }

    @Override
    public EventType type() {
        return EventType.ITEM_ADDED_TO_CART;
    }

    @Override
    public long version() {
        return version;
    }

    @Override
    public Map<String, String> asMap() {
        return Map.of(
                "userId", userId.toString(),
                "itemId", itemId.toString(),
                "quantity", String.valueOf(quantity),
                "version", String.valueOf(version),
                "eventType", type().name()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, itemId, quantity, version);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (ItemAddedToCart) object;
        return quantity == that.quantity &&
                version == that.version &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(itemId, that.itemId);
    }

    @Override
    public String toString() {
        return "ItemAddedToCart{" +
                "userId=" + userId +
                ", itemId=" + itemId +
                ", quantity=" + quantity +
                ", version=" + version +
                '}';
    }
}
