package com.github.xini1.usecase;

import com.github.xini1.event.item.ItemActivated;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemActivatedEventUseCase {

    void onEvent(ItemActivated itemActivated);
}
