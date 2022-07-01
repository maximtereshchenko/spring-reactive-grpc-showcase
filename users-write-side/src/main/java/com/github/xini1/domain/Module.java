package com.github.xini1.domain;

import com.github.xini1.usecase.AddItemUseCase;
import com.github.xini1.usecase.DisablePurchasingOfItemUseCase;
import com.github.xini1.usecase.EventStore;
import com.github.xini1.usecase.Identifiers;

/**
 * @author Maxim Tereshchenko
 */
public final class Module {

    private final ItemService itemService;

    private Module(ItemService itemService) {
        this.itemService = itemService;
    }

    public AddItemUseCase addItemUseCase() {
        return itemService;
    }

    public DisablePurchasingOfItemUseCase disablePurchasingOfItemUseCase() {
        return itemService;
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
            return new Module(new ItemService(eventStore, identifiers));
        }
    }
}
