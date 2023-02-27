package com.github.xini1.orders.read.usecase;

import com.github.xini1.common.event.cart.ItemRemovedFromCart;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemRemovedFromCartEventUseCase {

    void onEvent(ItemRemovedFromCart itemRemovedFromCart);
}
