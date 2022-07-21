package com.github.xini1.common.event.cart;

import com.github.xini1.common.event.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemsOrdered implements CartEvent {

    private final UUID userId;
    private final long version;

    public ItemsOrdered(UUID userId, long version) {
        this.userId = userId;
        this.version = version;
    }

    public ItemsOrdered(Map<String, String> properties) {
        this(
                UUID.fromString(properties.get("userId")),
                Long.parseLong(properties.get("version"))
        );
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }

    @Override
    public EventType type() {
        return EventType.ITEMS_ORDERED;
    }

    @Override
    public long version() {
        return version;
    }

    @Override
    public Map<String, String> asMap() {
        return Map.of(
                "userId", userId.toString(),
                "version", String.valueOf(version)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, version);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (ItemsOrdered) object;
        return version == that.version &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public String toString() {
        return "ItemsOrdered{" +
                "userId=" + userId +
                ", version=" + version +
                '}';
    }
}
