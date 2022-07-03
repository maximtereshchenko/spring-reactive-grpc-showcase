package com.github.xini1.domain;

import com.github.xini1.exception.UserIsNotRegular;
import com.github.xini1.usecase.AddItemToCartUseCase;
import com.github.xini1.usecase.User;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class CartService implements AddItemToCartUseCase {

    @Override
    public void add(UUID userId, User user, UUID item) {
        if (user != User.REGULAR) {
            throw new UserIsNotRegular();
        }
    }
}
