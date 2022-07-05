package com.github.xini1.domain;

import com.github.xini1.event.cart.ItemAddedToCart;
import com.github.xini1.exception.ItemIsNotFound;
import com.github.xini1.usecase.OnItemAddedToCartEventUseCase;
import com.github.xini1.usecase.OnItemCreatedEventUseCase;

/**
 * @author Maxim Tereshchenko
 */
final class UpdateService implements OnItemCreatedEventUseCase, OnItemAddedToCartEventUseCase {

    @Override
    public void onEvent(ItemAddedToCart itemAddedToCart) {
        throw new ItemIsNotFound();
    }
}
