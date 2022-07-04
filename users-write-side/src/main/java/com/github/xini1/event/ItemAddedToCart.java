package com.github.xini1.event;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemAddedToCart extends CartEvent {

    private final UUID itemId;

    public ItemAddedToCart(long version, UUID userId, UUID itemId) {
        super(version, userId);
        this.itemId = itemId;
    }

    public UUID itemId() {
        return itemId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), itemId);
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
        var that = (ItemAddedToCart) object;
        return Objects.equals(itemId, that.itemId);
    }

    @Override
    public String toString() {
        return "ItemAddedToCart{" +
                "itemId=" + itemId +
                "} " + super.toString();
    }
}
