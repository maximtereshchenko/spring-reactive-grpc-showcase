package com.github.xini1.usecase;

import com.github.xini1.User;
import com.github.xini1.view.OrderedItems;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewOrderedItemsUseCase {

    OrderedItems view(UUID userId, User admin);
}
