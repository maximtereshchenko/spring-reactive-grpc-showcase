package com.github.xini1.event;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemsOrdered extends CartEvent {

    private final Set<UUID> items;

    public ItemsOrdered(long version, UUID userId, Set<UUID> items) {
        super(version, userId);
        this.items = Set.copyOf(items);
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
