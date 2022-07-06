package com.github.xini1.usecase;

import com.github.xini1.event.cart.ItemRemovedFromCart;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemRemovedFromCartEventUseCase {

    void onEvent(ItemRemovedFromCart itemRemovedFromCart);
}
