package com.github.xini1.usecase;

import com.github.xini1.event.cart.ItemsOrdered;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemsOrderedEventUseCase {

    void onEvent(ItemsOrdered itemsOrdered);
}
