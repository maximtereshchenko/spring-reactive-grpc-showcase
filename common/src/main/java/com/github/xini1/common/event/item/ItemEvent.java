package com.github.xini1.common.event.item;

import com.github.xini1.common.event.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public abstract class ItemEvent extends VersionedEvent {

    private final UUID itemId;

    ItemEvent(long version, UUID itemId) {
        super(version);
        this.itemId = itemId;
    }

    @Override
    public UUID aggregateId() {
        return itemId;
    }

    @Override
    public EventType type() {
        return EventType.ITEM;
    }

    @Override
    public Map<String, String> asMap() {
        var map = new HashMap<>(super.asMap());
        map.put("itemId", itemId.toString());
        return Map.copyOf(map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var itemEvent = (ItemEvent) object;
        return Objects.equals(itemId, itemEvent.itemId);
    }

    @Override
    public String toString() {
        return "ItemEvent{" +
                "itemId=" + itemId +
                "} " + super.toString();
    }
}
