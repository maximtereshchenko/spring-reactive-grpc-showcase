package com.github.xini1.common.event;

import java.util.*;

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

    public static Collection<EventType> itemEvents() {
        return List.of(ITEM_ACTIVATED, ITEM_DEACTIVATED, ITEM_CREATED);
    }

    public static Collection<EventType> cartEvents() {
        return List.of(ITEM_ADDED_TO_CART, ITEM_REMOVED_FROM_CART, ITEMS_ORDERED);
    }
}
