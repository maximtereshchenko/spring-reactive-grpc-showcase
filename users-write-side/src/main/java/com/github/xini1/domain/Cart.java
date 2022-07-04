package com.github.xini1.domain;

import com.github.xini1.usecase.CartEvent;
import com.github.xini1.usecase.ItemAddedToCart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Maxim Tereshchenko
 */
final class Cart {

    private final UUID userId;
    private final List<UUID> items = new ArrayList<>();
    private final List<CartEvent> newEvents = new ArrayList<>();
    private final Map<Class<? extends CartEvent>, Consumer<CartEvent>> handlers = Map.of(
            ItemAddedToCart.class, cartEvent -> apply((ItemAddedToCart) cartEvent)
    );

    Cart(UUID userId) {
        this.userId = userId;
    }

    static Cart fromEvents(UUID userId, List<CartEvent> cartEvents) {
        var cart = new Cart(userId);
        cartEvents.forEach(cart::apply);
        cart.newEvents.clear();
        return cart;
    }

    void add(Item item) {
        apply(new ItemAddedToCart(userId, item.id()));
    }

    List<CartEvent> newEvents() {
        return List.copyOf(newEvents);
    }

    private void apply(CartEvent cartEvent) {
        handlers.get(cartEvent.getClass()).accept(cartEvent);
    }

    private void apply(ItemAddedToCart itemAddedToCart) {
        items.add(itemAddedToCart.itemId());
        newEvents.add(itemAddedToCart);
    }
}
