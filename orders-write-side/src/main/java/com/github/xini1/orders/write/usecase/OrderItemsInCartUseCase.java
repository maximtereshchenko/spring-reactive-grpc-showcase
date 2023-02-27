package com.github.xini1.orders.write.usecase;

import com.github.xini1.common.UserType;
import com.github.xini1.orders.write.exception.CartIsEmpty;
import com.github.xini1.orders.write.exception.UserIsNotRegular;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface OrderItemsInCartUseCase {

    void order(UUID userId, UserType userType) throws UserIsNotRegular, CartIsEmpty;
}
