package com.github.xini1.usecase;

import com.github.xini1.domain.Item;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewItemsUseCase {

    Iterable<Item> view();
}
