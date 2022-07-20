package com.github.xini1.common.event.cart;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemRemovedFromCart extends CartEvent {

    private final UUID itemId;
    private final int quantity;

    public ItemRemovedFromCart(long version, UUID userId, UUID itemId, int quantity) {
        super(version, userId);
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public ItemRemovedFromCart(Map<String, String> properties) {
        this(
                Long.parseLong(properties.get("version")),
                UUID.fromString(properties.get("userId")),
                UUID.fromString(properties.get("itemId")),
                Integer.parseInt(properties.get("quantity"))
        );
    }

    public UUID itemId() {
        return itemId;
    }

    public int quantity() {
        return quantity;
    }

    @Override
    public Map<String, String> asMap() {
        var map = new HashMap<>(super.asMap());
        map.put("itemId", itemId.toString());
        map.put("quantity", String.valueOf(quantity));
        return Map.copyOf(map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), itemId, quantity);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        var that = (ItemRemovedFromCart) object;
        return quantity == that.quantity &&
                Objects.equals(itemId, that.itemId);
    }

    @Override
    public String toString() {
        return "ItemRemovedFromCart{" +
                "itemId=" + itemId +
                ", quantity=" + quantity +
                "} " + super.toString();
    }
}
