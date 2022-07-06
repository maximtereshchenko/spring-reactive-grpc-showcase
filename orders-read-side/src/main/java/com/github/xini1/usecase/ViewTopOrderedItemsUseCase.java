package com.github.xini1.usecase;

import com.github.xini1.User;
import com.github.xini1.view.Item;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewTopOrderedItemsUseCase {

    Iterable<Item> view(User user);
}
