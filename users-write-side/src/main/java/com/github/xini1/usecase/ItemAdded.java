package com.github.xini1.usecase;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemAdded implements Event {

    private final long userId;
    private final long itemId;
    private final String name;

    public ItemAdded(long userId, long itemId, String name) {
        this.userId = userId;
        this.itemId = itemId;
        this.name = name;
    }
}
