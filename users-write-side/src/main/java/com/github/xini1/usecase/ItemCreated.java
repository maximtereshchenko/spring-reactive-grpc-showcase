package com.github.xini1.usecase;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemCreated implements ItemEvent {

    private final UUID userId;
    private final UUID itemId;
    private final String name;

    public ItemCreated(UUID userId, UUID itemId, String name) {
        this.userId = userId;
        this.itemId = itemId;
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, itemId, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var itemAdded = (ItemCreated) o;
        return Objects.equals(userId, itemAdded.userId) &&
                Objects.equals(itemId, itemAdded.itemId) &&
                Objects.equals(name, itemAdded.name);
    }

    @Override
    public String toString() {
        return "ItemCreated{" +
                "userId=" + userId +
                ", itemId=" + itemId +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public UUID itemId() {
        return itemId;
    }

    @Override
    public void accept(EventVisitor eventVisitor) {
        eventVisitor.visit(this);
    }
}
