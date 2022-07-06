package com.github.xini1.domain;

import com.github.xini1.port.ViewStore;
import com.github.xini1.usecase.OnItemAddedToCartEventUseCase;
import com.github.xini1.usecase.OnItemCreatedEventUseCase;
import com.github.xini1.usecase.OnItemRemovedFromCartEventUseCase;
import com.github.xini1.usecase.OnItemsOrderedEventUseCase;
import com.github.xini1.usecase.ViewCartUseCase;

/**
 * @author Maxim Tereshchenko
 */
public final class Module {

    private final ViewService viewService;
    private final UpdateService updateService;

    public Module(ViewStore viewStore) {
        viewService = new ViewService(viewStore);
        updateService = new UpdateService(viewStore);
    }

    public ViewCartUseCase viewCartUseCase() {
        return viewService;
    }

    public OnItemCreatedEventUseCase onItemCreatedEventUseCase() {
        return updateService;
    }

    public OnItemAddedToCartEventUseCase onItemAddedToCartEventUseCase() {
        return updateService;
    }

    public OnItemRemovedFromCartEventUseCase onItemRemovedFromCartEventUseCase() {
        return updateService;
    }

    public OnItemsOrderedEventUseCase onItemsOrderedEventUseCase() {
        return updateService;
    }
}
