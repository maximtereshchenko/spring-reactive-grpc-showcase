package com.github.xini1.orders.write.usecase;

import com.github.xini1.common.UserType;
import com.github.xini1.orders.write.exception.UserIsNotAdmin;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface CreateItemUseCase {

    UUID create(UUID userId, UserType userType, String name) throws UserIsNotAdmin;
}
