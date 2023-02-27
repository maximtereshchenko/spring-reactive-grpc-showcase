package com.github.xini1.orders.write.usecase;

import com.github.xini1.common.UserType;
import com.github.xini1.orders.write.exception.CouldNotAddDeactivatedItemToCart;
import com.github.xini1.orders.write.exception.ItemIsNotFound;
import com.github.xini1.orders.write.exception.QuantityIsNotPositive;
import com.github.xini1.orders.write.exception.UserIsNotRegular;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface AddItemToCartUseCase {

    void add(UUID userId, UserType userType, UUID itemId, int quantity)
            throws UserIsNotRegular, ItemIsNotFound, QuantityIsNotPositive, CouldNotAddDeactivatedItemToCart;
}
