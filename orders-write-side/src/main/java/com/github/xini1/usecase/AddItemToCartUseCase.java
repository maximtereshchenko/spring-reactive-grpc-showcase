package com.github.xini1.usecase;

import com.github.xini1.User;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface AddItemToCartUseCase {

    void add(UUID userId, User user, UUID itemId, int quantity);
}
