package com.github.xini1.common.event.item;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemDeactivated extends ItemEvent {

    private final UUID userId;

    public ItemDeactivated(long version, UUID userId, UUID itemId) {
        super(version, itemId);
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId);
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
        var that = (ItemDeactivated) object;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public String toString() {
        return "ItemDeactivated{" +
                "userId=" + userId +
                "} " + super.toString();
    }
}
