package com.github.xini1.orders.read.usecase;

import com.github.xini1.common.event.cart.ItemAddedToCart;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemAddedToCartEventUseCase {

    void onEvent(ItemAddedToCart itemAddedToCart);
}
