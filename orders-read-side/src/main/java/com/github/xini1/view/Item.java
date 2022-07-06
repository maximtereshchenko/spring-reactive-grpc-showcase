package com.github.xini1.view;

import com.github.xini1.event.item.ItemCreated;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public final class Item {

    private final UUID id;
    private final String name;
    private final boolean active;

    public Item(UUID id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public Item(ItemCreated itemCreated) {
        this(itemCreated.aggregateId(), itemCreated.name(), true);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, active);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var item = (Item) object;
        return active == item.active &&
                Objects.equals(id, item.id) &&
                Objects.equals(name, item.name);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
