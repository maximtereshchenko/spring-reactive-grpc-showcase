package com.github.xini1.usecase;

import com.github.xini1.User;
import com.github.xini1.view.TopOrderedItem;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewTopOrderedItemsUseCase {

    Iterable<TopOrderedItem> view(User user);
}
