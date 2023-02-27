package com.github.xini1.orders.read.domain;

import com.github.xini1.orders.read.port.ViewStore;
import com.github.xini1.orders.read.usecase.*;

import java.time.Clock;

/**
 * @author Maxim Tereshchenko
 */
public final class Module {

    private final ViewService viewService;
    private final UpdateService updateService;

    public Module(ViewStore viewStore, Clock clock) {
        viewService = new ViewService(viewStore);
        updateService = new UpdateService(viewStore, clock);
    }

    public Module(ViewStore viewStore) {
        this(viewStore, Clock.systemDefaultZone());
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

    public ViewItemsUseCase viewItemsUseCase() {
        return viewService;
    }

    public OnItemDeactivatedEventUseCase onItemDeactivatedEventUseCase() {
        return updateService;
    }

    public OnItemActivatedEventUseCase onItemActivatedEventUseCase() {
        return updateService;
    }

    public ViewTopOrderedItemsUseCase viewTopOrderedItemsUseCase() {
        return viewService;
    }

    public ViewOrderedItemsUseCase viewOrderedItemsUseCase() {
        return viewService;
    }
}
