package com.github.xini1.usecase;

import com.github.xini1.event.item.ItemCreated;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemCreatedEventUseCase {

    void onEvent(ItemCreated itemCreated);
}
