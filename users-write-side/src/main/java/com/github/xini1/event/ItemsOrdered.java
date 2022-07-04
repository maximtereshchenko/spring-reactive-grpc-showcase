package com.github.xini1.event;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemsOrdered extends CartEvent {

    public ItemsOrdered(long version, UUID userId) {
        super(version, userId);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return super.equals(object);
    }

    @Override
    public String toString() {
        return "ItemsOrdered{} " + super.toString();
    }
}
