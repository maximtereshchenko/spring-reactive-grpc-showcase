package com.github.xini1.usecase;

import com.github.xini1.User;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface OrderItemsInCartUseCase {

    void order(UUID userId, User user);
}
