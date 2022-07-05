package com.github.xini1.usecase;

import com.github.xini1.event.cart.ItemAddedToCart;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemAddedToCartEventUseCase {

    void onEvent(ItemAddedToCart itemAddedToCart);
}
