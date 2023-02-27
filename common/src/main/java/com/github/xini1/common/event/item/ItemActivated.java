package com.github.xini1.common.event.item;

import com.github.xini1.common.event.EventType;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemActivated implements ItemEvent {

    private final UUID itemId;
    private final UUID userId;
    private final long version;

    public ItemActivated(UUID itemId, UUID userId, long version) {
        this.itemId = itemId;
        this.userId = userId;
        this.version = version;
    }

    public ItemActivated(Map<String, String> properties) {
        this(
                UUID.fromString(properties.get("itemId")),
                UUID.fromString(properties.get("userId")),
                Long.parseLong(properties.get("version"))
        );
    }

    public UUID userId() {
        return userId;
    }

    @Override
    public UUID aggregateId() {
        return itemId;
    }

    @Override
    public EventType type() {
        return EventType.ITEM_ACTIVATED;
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
                "version", String.valueOf(version),
                "eventType", type().name()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, userId, version);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (ItemActivated) object;
        return version == that.version &&
                Objects.equals(itemId, that.itemId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public String toString() {
        return "ItemActivated{" +
                "itemId=" + itemId +
                ", userId=" + userId +
                ", version=" + version +
                '}';
    }
}
