package com.github.xini1.common.event.item;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemCreated extends ItemEvent {

    private final UUID userId;
    private final String name;

    public ItemCreated(long version, UUID userId, UUID itemId, String name) {
        super(version, itemId);
        this.userId = userId;
        this.name = name;
    }

    public ItemCreated(Map<String, String> properties) {
        this(
                Long.parseLong(properties.get("version")),
                UUID.fromString(properties.get("userId")),
                UUID.fromString(properties.get("itemId")),
                properties.get("name")
        );
    }

    public String name() {
        return name;
    }

    @Override
    public Map<String, String> asMap() {
        var map = new HashMap<>(super.asMap());
        map.put("userId", userId.toString());
        map.put("name", name);
        return Map.copyOf(map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId, name);
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
        var that = (ItemCreated) object;
        return Objects.equals(userId, that.userId) && Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return "ItemCreated{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                "} " + super.toString();
    }
}
