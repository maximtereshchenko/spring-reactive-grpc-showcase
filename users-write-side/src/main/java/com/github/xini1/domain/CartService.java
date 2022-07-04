package com.github.xini1.domain;

import com.github.xini1.exception.CouldNotAddDeactivatedItemToCart;
import com.github.xini1.exception.UserIsNotRegular;
import com.github.xini1.usecase.AddItemToCartUseCase;
import com.github.xini1.usecase.User;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class CartService implements AddItemToCartUseCase {

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
        var item = items.find(itemId)
                .orElseThrow();
        if (item.isDeactivated()) {
            throw new CouldNotAddDeactivatedItemToCart();
        }
        var cart = carts.find(userId);
        cart.add(item);
        carts.save(cart);
    }
}
