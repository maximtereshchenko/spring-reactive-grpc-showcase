package com.github.xini1.orders.read.usecase;

import com.github.xini1.common.UserType;
import com.github.xini1.orders.read.exception.UserIsNotAdmin;
import com.github.xini1.orders.read.view.TopOrderedItem;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewTopOrderedItemsUseCase {

    Iterable<TopOrderedItem> view(UserType userType) throws UserIsNotAdmin;
}
