package com.github.xini1.domain;

import com.github.xini1.usecase.EventStore;

import java.util.Optional;
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

    Optional<Item> find(UUID itemId) {
        return Item.fromEvents(eventStore.findById(itemId));
    }
}
