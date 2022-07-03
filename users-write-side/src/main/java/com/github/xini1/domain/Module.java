package com.github.xini1.domain;

import com.github.xini1.usecase.CreateItemUseCase;
import com.github.xini1.usecase.DeactivateItemUseCase;
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

    public CreateItemUseCase createItemUseCase() {
        return itemService;
    }

    public DeactivateItemUseCase deactivateItemUseCase() {
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
            return new Module(new ItemService(new Items(eventStore), identifiers));
        }
    }
}
