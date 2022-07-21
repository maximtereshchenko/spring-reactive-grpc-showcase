package com.github.xini1.orders.write.domain;

import com.github.xini1.common.*;
import com.github.xini1.orders.write.exception.*;
import com.github.xini1.orders.write.port.*;
import com.github.xini1.orders.write.usecase.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class CartService implements AddItemToCartUseCase, OrderItemsInCartUseCase, RemoveItemFromCartUseCase {

    private final EventStore eventStore;

    CartService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void add(UUID userId, UserType userType, UUID itemId, int quantity)
            throws UserIsNotRegular, ItemIsNotFound, QuantityIsNotPositive, CouldNotAddDeactivatedItemToCart {
        if (userType != UserType.REGULAR) {
            throw new UserIsNotRegular();
        }
        var cart = Cart.fromEvents(userId, eventStore);
        cart.add(Item.fromEvents(itemId, eventStore), quantity);
        cart.save(eventStore);
    }

    @Override
    public void order(UUID userId, UserType userType) throws UserIsNotRegular, CartIsEmpty {
        if (userType != UserType.REGULAR) {
            throw new UserIsNotRegular();
        }
        var cart = Cart.fromEvents(userId, eventStore);
        cart.order(eventStore);
        cart.save(eventStore);
    }

    @Override
    public void remove(UUID userId, UserType userType, UUID itemId, int quantity)
            throws UserIsNotRegular, ItemIsNotFound, QuantityIsNotPositive, QuantityIsMoreThanCartHas {
        if (userType != UserType.REGULAR) {
            throw new UserIsNotRegular();
        }
        var cart = Cart.fromEvents(userId, eventStore);
        cart.remove(Item.fromEvents(itemId, eventStore), quantity);
        cart.save(eventStore);
    }
}
