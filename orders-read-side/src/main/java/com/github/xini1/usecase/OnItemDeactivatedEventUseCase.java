package com.github.xini1.usecase;

import com.github.xini1.event.item.ItemDeactivated;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemDeactivatedEventUseCase {

    void onEvent(ItemDeactivated itemDeactivated);
}
