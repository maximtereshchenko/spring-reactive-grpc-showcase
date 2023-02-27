package com.github.xini1.orders.read.usecase;

import com.github.xini1.common.event.item.ItemCreated;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemCreatedEventUseCase {

    void onEvent(ItemCreated itemCreated);
}
