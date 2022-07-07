package com.github.xini1.orders.read.usecase;

import com.github.xini1.common.event.cart.*;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemsOrderedEventUseCase {

    void onEvent(ItemsOrdered itemsOrdered);
}
