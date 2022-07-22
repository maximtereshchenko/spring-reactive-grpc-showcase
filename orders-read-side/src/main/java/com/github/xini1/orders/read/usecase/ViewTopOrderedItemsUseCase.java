package com.github.xini1.orders.read.usecase;

import com.github.xini1.common.*;
import com.github.xini1.orders.read.exception.*;
import com.github.xini1.orders.read.view.*;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewTopOrderedItemsUseCase {

    Iterable<TopOrderedItem> view(UserType userType) throws UserIsNotAdmin;
}
