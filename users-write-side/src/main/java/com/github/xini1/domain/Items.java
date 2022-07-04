package com.github.xini1.domain;

import com.github.xini1.exception.ItemIsNotFound;
import com.github.xini1.port.EventStore;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class Items {

    private final EventStore eventStore;

    Items(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    void save(Item item) {
        item.newEvents()
                .forEach(eventStore::publish);
    }

    Item find(UUID itemId) {
        return Item.fromEvents(itemId, eventStore.itemEvents(itemId))
                .orElseThrow(ItemIsNotFound::new);
    }
}
