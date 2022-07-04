package com.github.xini1.domain;

import com.github.xini1.exception.ItemHasNotBeenCreated;
import com.github.xini1.exception.ItemIsAlreadyActive;
import com.github.xini1.exception.ItemIsAlreadyDeactivated;
import com.github.xini1.usecase.Identifiers;
import com.github.xini1.usecase.ItemActivated;
import com.github.xini1.usecase.ItemCreated;
import com.github.xini1.usecase.ItemDeactivated;
import com.github.xini1.usecase.ItemEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class Item extends AggregateRoot {

    private State state = new Initial();

    Item() {
        register(ItemCreated.class, this::onEvent);
        register(ItemDeactivated.class, this::onEvent);
        register(ItemActivated.class, this::onEvent);
    }

    static Item create(UUID userId, String name, Identifiers identifiers) {
        var item = new Item();
        item.apply(new ItemCreated(userId, identifiers.newIdentifier(), name));
        return item;
    }

    static Optional<Item> fromEvents(List<ItemEvent> events) {
        if (events.isEmpty()) {
            return Optional.empty();
        }
        var item = new Item();
        events.forEach(item::apply);
        item.clearEvents();
        return Optional.of(item);
    }

    UUID id() {
        return state.id();
    }

    void deactivate(UUID userId) {
        state.onDeactivation();
        apply(new ItemDeactivated(userId, state.id()));
    }

    void activate(UUID userId) {
        state.onActivation();
        apply(new ItemActivated(userId, state.id()));
    }

    boolean isDeactivated() {
        return state.isDeactivated();
    }

    private void onEvent(ItemCreated itemCreated) {
        state = new Active(itemCreated.itemId());
    }

    private void onEvent(ItemDeactivated itemDeactivated) {
        state = new Deactivated(itemDeactivated.itemId());
    }

    private void onEvent(ItemActivated itemActivated) {
        state = new Active(itemActivated.itemId());
    }

    private interface State {

        UUID id();

        void onActivation();

        void onDeactivation();

        boolean isDeactivated();
    }

    private static final class Initial implements State {

        @Override
        public UUID id() {
            throw new ItemHasNotBeenCreated();
        }

        @Override
        public void onActivation() {
            throw new ItemHasNotBeenCreated();
        }

        @Override
        public void onDeactivation() {
            throw new ItemHasNotBeenCreated();
        }

        @Override
        public boolean isDeactivated() {
            throw new ItemHasNotBeenCreated();
        }
    }

    private static final class Active implements State {

        private final UUID id;

        private Active(UUID id) {
            this.id = id;
        }

        @Override
        public UUID id() {
            return id;
        }

        @Override
        public void onActivation() {
            throw new ItemIsAlreadyActive();
        }

        @Override
        public void onDeactivation() {
            //empty
        }

        @Override
        public boolean isDeactivated() {
            return false;
        }
    }

    private static final class Deactivated implements State {

        private final UUID id;

        private Deactivated(UUID id) {
            this.id = id;
        }

        @Override
        public UUID id() {
            return id;
        }

        @Override
        public void onActivation() {
            //empty
        }

        @Override
        public void onDeactivation() {
            throw new ItemIsAlreadyDeactivated();
        }

        @Override
        public boolean isDeactivated() {
            return true;
        }
    }
}
