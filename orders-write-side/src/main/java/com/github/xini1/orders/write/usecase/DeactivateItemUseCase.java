package com.github.xini1.orders.write.usecase;

import com.github.xini1.common.*;
import com.github.xini1.orders.write.exception.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface DeactivateItemUseCase {

    void deactivate(UUID userId, UserType userType, UUID itemId)
            throws UserIsNotAdmin, ItemIsAlreadyDeactivated, ItemIsNotFound;
}
