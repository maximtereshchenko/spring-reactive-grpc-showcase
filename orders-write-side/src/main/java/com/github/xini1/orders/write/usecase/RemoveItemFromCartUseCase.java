package com.github.xini1.orders.write.usecase;

import com.github.xini1.common.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface RemoveItemFromCartUseCase {

    void remove(UUID userId, UserType userType, UUID itemId, int quantity);
}
