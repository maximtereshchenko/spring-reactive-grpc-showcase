package com.github.xini1.usecase;

import com.github.xini1.*;
import com.github.xini1.view.*;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewTopOrderedItemsUseCase {

    Iterable<TopOrderedItem> view(User user);
}
