package com.github.xini1.common.event.cart;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemsOrdered extends CartEvent {

    public ItemsOrdered(long version, UUID userId) {
        super(version, userId);
    }

    public ItemsOrdered(Map<String, String> properties) {
        this(
                Long.parseLong(properties.get("version")),
                UUID.fromString(properties.get("userId"))
        );
    }

    @Override
    public String toString() {
        return "ItemsOrdered{} " + super.toString();
    }
}
