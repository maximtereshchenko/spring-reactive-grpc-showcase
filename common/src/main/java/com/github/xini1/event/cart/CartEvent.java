package com.github.xini1.event.cart;

import com.github.xini1.event.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public abstract class CartEvent extends VersionedEvent {

    private final UUID userId;

    CartEvent(long version, UUID userId) {
        super(version);
        this.userId = userId;
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }

    @Override
    public EventType type() {
        return EventType.CART;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var cartEvent = (CartEvent) object;
        return Objects.equals(userId, cartEvent.userId);
    }

    @Override
    public String toString() {
        return "CartEvent{" +
                "userId=" + userId +
                '}';
    }
}
