package com.github.xini1.domain;

import com.github.xini1.usecase.OnItemAddedToCartEventUseCase;
import com.github.xini1.usecase.OnItemCreatedEventUseCase;
import com.github.xini1.usecase.ViewCartUseCase;

/**
 * @author Maxim Tereshchenko
 */
public final class Module {

    private final ViewService viewService;

    public Module() {
        viewService = new ViewService();
    }

    public ViewCartUseCase viewCartUseCase() {
        return viewService;
    }

    public OnItemCreatedEventUseCase onItemCreatedEventUseCase() {
        return null;
    }

    public OnItemAddedToCartEventUseCase onItemAddedToCartEventUseCase() {
        return null;
    }
}
