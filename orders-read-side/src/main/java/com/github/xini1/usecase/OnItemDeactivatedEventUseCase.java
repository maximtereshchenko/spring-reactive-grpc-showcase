package com.github.xini1.usecase;

import com.github.xini1.event.item.*;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemDeactivatedEventUseCase {

    void onEvent(ItemDeactivated itemDeactivated);
}
