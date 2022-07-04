package com.github.xini1.usecase;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface OrderItemsInCartUseCase {

    void order(UUID userId, User user);
}
