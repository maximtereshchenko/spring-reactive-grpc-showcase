package com.github.xini1.view;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public final class TopOrderedItem {

    private final UUID id;
    private final String name;
    private final long ordered;

    public TopOrderedItem(UUID id, String name, long ordered) {
        this.id = id;
        this.name = name;
        this.ordered = ordered;
    }
}
