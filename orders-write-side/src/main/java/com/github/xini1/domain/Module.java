package com.github.xini1.domain;

import com.github.xini1.port.EventStore;
import com.github.xini1.usecase.ActivateItemUseCase;
import com.github.xini1.usecase.AddItemToCartUseCase;
import com.github.xini1.usecase.CreateItemUseCase;
import com.github.xini1.usecase.DeactivateItemUseCase;
import com.github.xini1.usecase.OrderItemsInCartUseCase;
import com.github.xini1.usecase.RemoveItemFromCartUseCase;

/**
 * @author Maxim Tereshchenko
 */
public final class Module {

    private final ItemService itemService;
    private final CartService cartService;

    public Module(EventStore eventStore) {
        this.itemService = new ItemService(eventStore);
        this.cartService = new CartService(eventStore);
    }

    public CreateItemUseCase createItemUseCase() {
        return itemService;
    }

    public DeactivateItemUseCase deactivateItemUseCase() {
        return itemService;
    }

    public ActivateItemUseCase activateItemUseCase() {
        return itemService;
    }

    public AddItemToCartUseCase addItemToCartUseCase() {
        return cartService;
    }

    public OrderItemsInCartUseCase orderItemsInCartUseCase() {
        return cartService;
    }

    public RemoveItemFromCartUseCase removeItemFromCartUseCase() {
        return null;
    }
}
