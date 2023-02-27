package com.github.xini1.orders.write.usecase;

import com.github.xini1.common.UserType;
import com.github.xini1.orders.write.exception.ItemIsAlreadyActive;
import com.github.xini1.orders.write.exception.ItemIsNotFound;
import com.github.xini1.orders.write.exception.UserIsNotAdmin;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface ActivateItemUseCase {

    void activate(UUID userId, UserType userType, UUID itemId)
            throws UserIsNotAdmin, ItemIsNotFound, ItemIsAlreadyActive;
}
