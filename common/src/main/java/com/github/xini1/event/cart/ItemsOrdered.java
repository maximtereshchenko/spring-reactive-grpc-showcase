package com.github.xini1.event.cart;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemsOrdered extends CartEvent {

    public ItemsOrdered(long version, UUID userId) {
        super(version, userId);
    }

    @Override
    public String toString() {
        return "ItemsOrdered{} " + super.toString();
    }
}
