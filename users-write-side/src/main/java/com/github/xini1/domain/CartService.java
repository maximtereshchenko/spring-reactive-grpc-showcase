package com.github.xini1.domain;

import com.github.xini1.exception.ItemIsNotFound;
import com.github.xini1.exception.UserIsNotRegular;
import com.github.xini1.usecase.AddItemToCartUseCase;
import com.github.xini1.usecase.OrderItemsInCartUseCase;
import com.github.xini1.usecase.User;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class CartService implements AddItemToCartUseCase, OrderItemsInCartUseCase {

    private final Items items;
    private final Carts carts;

    CartService(Items items, Carts carts) {
        this.items = items;
        this.carts = carts;
    }

    @Override
    public void add(UUID userId, User user, UUID itemId) {
        if (user != User.REGULAR) {
            throw new UserIsNotRegular();
        }
        var cart = carts.find(userId);
        cart.add(
                items.find(itemId)
                        .orElseThrow(ItemIsNotFound::new)
        );
        carts.save(cart);
    }

    @Override
    public void order(UUID userId, User user) {
        var cart = carts.find(userId);
        cart.order();
        carts.save(cart);
    }
}
