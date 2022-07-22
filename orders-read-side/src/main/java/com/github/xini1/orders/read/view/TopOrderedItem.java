package com.github.xini1.orders.read.view;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class TopOrderedItem {

    private final UUID id;
    private final String name;
    private final long timesOrdered;

    public TopOrderedItem(UUID id, String name, long timesOrdered) {
        this.id = id;
        this.name = name;
        this.timesOrdered = timesOrdered;
    }

    public TopOrderedItem(Item item) {
        this(item.getId(), item.getName(), 0);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getTimesOrdered() {
        return timesOrdered;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, timesOrdered);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (TopOrderedItem) object;
        return timesOrdered == that.timesOrdered &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return "TopOrderedItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", timesOrdered=" + timesOrdered +
                '}';
    }

    public TopOrderedItem addTimesOrdered(int quantity) {
        return new TopOrderedItem(id, name, timesOrdered + quantity);
    }
}
