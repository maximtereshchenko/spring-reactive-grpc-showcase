package com.github.xini1.usecase;

import com.github.xini1.*;
import com.github.xini1.view.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewOrderedItemsUseCase {

    OrderedItems viewOrderedItems(UUID userId, User user);
}
