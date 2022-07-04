package com.github.xini1.domain;

import com.github.xini1.usecase.CartEvent;
import com.github.xini1.usecase.ItemAddedToCart;

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
        addHandler(ItemAddedToCart.class, this::apply);
    }

    static Cart fromEvents(UUID userId, List<CartEvent> cartEvents) {
        var cart = new Cart(userId);
        cartEvents.forEach(cart::apply);
        cart.clearEvents();
        return cart;
    }

    void add(Item item) {
        super.apply(new ItemAddedToCart(userId, item.id()));
    }

    private void apply(ItemAddedToCart itemAddedToCart) {
        items.add(itemAddedToCart.itemId());
    }
}
