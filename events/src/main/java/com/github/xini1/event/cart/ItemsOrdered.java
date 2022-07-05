package com.github.xini1.event.cart;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemsOrdered extends CartEvent {

    private final Map<UUID, Integer> items;

    public ItemsOrdered(long version, UUID userId, Map<UUID, Integer> items) {
        super(version, userId);
        this.items = Map.copyOf(items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), items);
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
        var that = (ItemsOrdered) object;
        return Objects.equals(items, that.items);
    }

    @Override
    public String toString() {
        return "ItemsOrdered{" +
                "items=" + items +
                "} " + super.toString();
    }
}
