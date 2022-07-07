package com.github.xini1.orders.read.usecase;

import com.github.xini1.orders.read.view.*;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewItemsUseCase {

    Iterable<Item> view();
}
