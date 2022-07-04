package com.github.xini1.domain;

import com.github.xini1.usecase.ActivateItemUseCase;
import com.github.xini1.usecase.AddItemToCartUseCase;
import com.github.xini1.usecase.CreateItemUseCase;
import com.github.xini1.usecase.DeactivateItemUseCase;
import com.github.xini1.usecase.EventStore;
import com.github.xini1.usecase.Identifiers;

/**
 * @author Maxim Tereshchenko
 */
public final class Module {

    private final ItemService itemService;
    private final CartService cartService;

    private Module(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
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

    public static final class Builder {

        private EventStore eventStore;
        private Identifiers identifiers;

        public Builder with(EventStore eventStore) {
            this.eventStore = eventStore;
            return this;
        }

        public Builder with(Identifiers identifiers) {
            this.identifiers = identifiers;
            return this;
        }

        public Module build() {
            var items = new Items(eventStore);
            return new Module(new ItemService(items, identifiers), new CartService(items, new Carts(eventStore)));
        }
    }
}
