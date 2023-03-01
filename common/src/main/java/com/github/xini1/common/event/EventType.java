package com.github.xini1.common.event;

import java.util.Set;

/**
 * @author Maxim Tereshchenko
 */
public enum EventType {

    ITEM_ACTIVATED,
    ITEM_CREATED,
    ITEM_DEACTIVATED,
    USER_REGISTERED,
    ITEM_ADDED_TO_CART,
    ITEM_REMOVED_FROM_CART,
    ITEMS_ORDERED;

    public static Set<EventType> itemEvents() {
        return Set.of(ITEM_ACTIVATED, ITEM_DEACTIVATED, ITEM_CREATED);
    }

    public static Set<EventType> cartEvents() {
        return Set.of(ITEM_ADDED_TO_CART, ITEM_REMOVED_FROM_CART, ITEMS_ORDERED);
    }
}
