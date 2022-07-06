package com.github.xini1.usecase;

import com.github.xini1.event.cart.*;

/**
 * @author Maxim Tereshchenko
 */
public interface OnItemAddedToCartEventUseCase {

    void onEvent(ItemAddedToCart itemAddedToCart);
}
