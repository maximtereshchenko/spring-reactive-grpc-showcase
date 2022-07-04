package com.github.xini1.domain;

import com.github.xini1.event.cart.ItemAddedToCart;
import com.github.xini1.event.cart.ItemsOrdered;
import com.github.xini1.exception.CartIsEmpty;
import com.github.xini1.exception.CouldNotAddDeactivatedItemToCart;
import com.github.xini1.port.EventStore;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class Cart extends AggregateRoot {

    private final Set<UUID> items = new HashSet<>();

    Cart(UUID userId) {
        super(userId);
        register(ItemAddedToCart.class, this::onEvent);
        register(ItemsOrdered.class, this::onEvent);
    }

    static Cart fromEvents(UUID userId, EventStore eventStore) {
        var cart = new Cart(userId);
        eventStore.cartEvents(userId)
                .forEach(cart::apply);
        cart.clearEvents();
        return cart;
    }

    void add(Item item) {
        if (item.isDeactivated()) {
            throw new CouldNotAddDeactivatedItemToCart();
        }
        apply(new ItemAddedToCart(nextVersion(), id(), item.id()));
    }

    void order() {
        if (items.isEmpty()) {
            throw new CartIsEmpty();
        }
        apply(new ItemsOrdered(nextVersion(), id(), items));
    }

    private void onEvent(ItemAddedToCart itemAddedToCart) {
        items.add(itemAddedToCart.itemId());
    }

    private void onEvent(ItemsOrdered itemsOrdered) {
        items.clear();
    }
}
