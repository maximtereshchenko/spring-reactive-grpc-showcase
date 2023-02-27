package com.github.xini1.orders.write.usecase;

import com.github.xini1.common.UserType;
import com.github.xini1.orders.write.exception.ItemIsAlreadyDeactivated;
import com.github.xini1.orders.write.exception.ItemIsNotFound;
import com.github.xini1.orders.write.exception.UserIsNotAdmin;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface DeactivateItemUseCase {

    void deactivate(UUID userId, UserType userType, UUID itemId)
            throws UserIsNotAdmin, ItemIsAlreadyDeactivated, ItemIsNotFound;
}
