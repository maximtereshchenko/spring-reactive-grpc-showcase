package com.github.xini1.orders.read.usecase;

import com.github.xini1.common.UserType;
import com.github.xini1.orders.read.exception.UserIsNotRegular;
import com.github.xini1.orders.read.view.OrderedItems;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewOrderedItemsUseCase {

    OrderedItems viewOrderedItems(UUID userId, UserType userType) throws UserIsNotRegular;
}
