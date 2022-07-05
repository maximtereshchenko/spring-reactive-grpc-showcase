package com.github.xini1.event.item;

import java.util.Objects;
import java.util.UUID;

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
