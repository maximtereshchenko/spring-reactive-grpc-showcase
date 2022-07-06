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

    public Item(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public Item(ItemCreated itemCreated) {
        this(itemCreated.aggregateId(), itemCreated.name());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var itemView = (Item) object;
        return Objects.equals(id, itemView.id) &&
                Objects.equals(name, itemView.name);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
