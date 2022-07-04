package com.github.xini1.domain;

import com.github.xini1.port.EventStore;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class Carts {

    private final EventStore eventStore;

    Carts(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    Cart find(UUID userId) {
        return Cart.fromEvents(userId, eventStore.cartEvents(userId));
    }

    void save(Cart cart) {
        cart.newEvents()
                .forEach(eventStore::publish);
    }
}
