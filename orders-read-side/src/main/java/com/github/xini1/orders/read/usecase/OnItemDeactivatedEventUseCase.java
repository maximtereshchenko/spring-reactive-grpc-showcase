package com.github.xini1.orders.read.usecase;

import com.github.xini1.common.event.item.*;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemDeactivatedEventUseCase {

    void onEvent(ItemDeactivated itemDeactivated);
}
