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
    private final long version;

    public Item(UUID id, String name, boolean active, long version) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.version = version;
    }

    public Item(ItemCreated itemCreated) {
        this(itemCreated.aggregateId(), itemCreated.name(), true, itemCreated.version());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getVersion() {
        return version;
    }

    public boolean isActive() {
        return active;
    }

    public Item deactivated(long version) {
        return new Item(id, name, false, version);
    }

    public Item activated(long version) {
        return new Item(id, name, true, version);
    }

    public boolean hasVersionLessThan(long version) {
        return this.version < version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, active, version);
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
                version == item.version &&
                Objects.equals(id, item.id) &&
                Objects.equals(name, item.name);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                ", version=" + version +
                '}';
    }
}
