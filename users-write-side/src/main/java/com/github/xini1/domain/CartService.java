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

    CartService(Items items) {
        this.items = items;
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
    }
}
