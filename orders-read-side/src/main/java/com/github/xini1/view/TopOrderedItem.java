package com.github.xini1.view;

import java.util.UUID;

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

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getTimesOrdered() {
        return timesOrdered;
    }
}
