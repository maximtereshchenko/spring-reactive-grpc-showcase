package com.github.xini1.common.event.item;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemActivated extends ItemEvent {

    private final UUID userId;

    public ItemActivated(long version, UUID userId, UUID itemId) {
        super(version, itemId);
        this.userId = userId;
    }

    public ItemActivated(Map<String, String> properties) {
        this(
                Long.parseLong(properties.get("version")),
                UUID.fromString(properties.get("userId")),
                UUID.fromString(properties.get("itemId"))
        );
    }

    public UUID userId() {
        return userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId);
    }

    @Override
    public Map<String, String> asMap() {
        var map = new HashMap<>(super.asMap());
        map.put("userId", userId.toString());
        return Map.copyOf(map);
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
        var that = (ItemActivated) object;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public String toString() {
        return "ItemActivated{" +
                "userId=" + userId +
                "} " + super.toString();
    }
}
