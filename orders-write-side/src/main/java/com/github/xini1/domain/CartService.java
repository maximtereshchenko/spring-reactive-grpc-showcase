package com.github.xini1.domain;

import com.github.xini1.User;
import com.github.xini1.exception.UserIsNotRegular;
import com.github.xini1.port.EventStore;
import com.github.xini1.usecase.AddItemToCartUseCase;
import com.github.xini1.usecase.OrderItemsInCartUseCase;
import com.github.xini1.usecase.RemoveItemFromCartUseCase;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class CartService implements AddItemToCartUseCase, OrderItemsInCartUseCase, RemoveItemFromCartUseCase {

    private final EventStore eventStore;

    CartService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void add(UUID userId, User user, UUID itemId, int quantity) {
        if (user != User.REGULAR) {
            throw new UserIsNotRegular();
        }
        var cart = Cart.fromEvents(userId, eventStore);
        cart.add(Item.fromEvents(itemId, eventStore), quantity);
        cart.save(eventStore);
    }

    @Override
    public void order(UUID userId, User user) {
        if (user != User.REGULAR) {
            throw new UserIsNotRegular();
        }
        var cart = Cart.fromEvents(userId, eventStore);
        cart.order(eventStore);
        cart.save(eventStore);
    }

    @Override
    public void remove(UUID userId, User user, UUID itemId, int quantity) {
        if (user != User.REGULAR) {
            throw new UserIsNotRegular();
        }
        var cart = Cart.fromEvents(userId, eventStore);
        cart.remove(Item.fromEvents(itemId, eventStore), quantity);
        cart.save(eventStore);
    }
}
