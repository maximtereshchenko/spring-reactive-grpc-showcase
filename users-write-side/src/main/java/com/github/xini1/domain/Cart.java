package com.github.xini1.domain;

import com.github.xini1.event.CartEvent;
import com.github.xini1.event.ItemAddedToCart;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class Cart extends AggregateRoot {

    private final UUID userId;
    private final List<UUID> items = new ArrayList<>();

    Cart(UUID userId) {
        this.userId = userId;
        register(ItemAddedToCart.class, this::onEvent);
    }

    static Cart fromEvents(UUID userId, List<CartEvent> cartEvents) {
        var cart = new Cart(userId);
        cartEvents.forEach(cart::apply);
        cart.clearEvents();
        return cart;
    }

    void add(Item item) {
        apply(new ItemAddedToCart(nextVersion(), userId, item.id()));
    }

    private void onEvent(ItemAddedToCart itemAddedToCart) {
        items.add(itemAddedToCart.itemId());
    }
}
