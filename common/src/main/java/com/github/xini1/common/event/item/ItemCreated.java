package com.github.xini1.common.event.item;

import com.github.xini1.common.event.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemCreated implements ItemEvent {

    private final UUID itemId;
    private final UUID userId;
    private final String name;
    private final long version;

    public ItemCreated(UUID itemId, UUID userId, String name, long version) {
        this.itemId = itemId;
        this.userId = userId;
        this.name = name;
        this.version = version;
    }

    public ItemCreated(Map<String, String> properties) {
        this(
                UUID.fromString(properties.get("itemId")),
                UUID.fromString(properties.get("userId")),
                properties.get("name"),
                Long.parseLong(properties.get("version"))
        );
    }

    public String name() {
        return name;
    }

    @Override
    public UUID aggregateId() {
        return itemId;
    }

    @Override
    public EventType type() {
        return EventType.ITEM_CREATED;
    }

    @Override
    public long version() {
        return version;
    }

    @Override
    public Map<String, String> asMap() {
        return Map.of(
                "itemId", itemId.toString(),
                "userId", userId.toString(),
                "name", name,
                "version", String.valueOf(version),
                "eventType", type().name()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, userId, name, version);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (ItemCreated) object;
        return version == that.version &&
                Objects.equals(itemId, that.itemId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return "ItemCreated{" +
                "itemId=" + itemId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", version=" + version +
                '}';
    }
}
