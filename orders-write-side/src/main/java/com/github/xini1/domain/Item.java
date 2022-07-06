package com.github.xini1.domain;

import com.github.xini1.event.item.*;
import com.github.xini1.exception.*;
import com.github.xini1.port.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class Item extends AggregateRoot {

    private State state = new Active();

    Item(UUID itemId) {
        super(itemId);
        register(ItemCreated.class, event -> {});
        register(ItemDeactivated.class, event -> state = new Deactivated());
        register(ItemActivated.class, event -> state = new Active());
    }

    static Item create(UUID userId, String name) {
        var item = new Item(UUID.randomUUID());
        item.apply(new ItemCreated(item.nextVersion(), userId, item.id(), name));
        return item;
    }

    static Item fromEvents(UUID itemId, EventStore eventStore) {
        var events = eventStore.itemEvents(itemId);
        if (events.isEmpty()) {
            throw new ItemIsNotFound();
        }
        var item = new Item(itemId);
        events.forEach(item::apply);
        item.clearEvents();
        return item;
    }

    void deactivate(UUID userId) {
        state.onDeactivation();
        apply(new ItemDeactivated(nextVersion(), userId, id()));
    }

    void activate(UUID userId) {
        state.onActivation();
        apply(new ItemActivated(nextVersion(), userId, id()));
    }

    boolean isDeactivated() {
        return state.isDeactivated();
    }

    private interface State {

        void onActivation();

        void onDeactivation();

        boolean isDeactivated();
    }

    private static final class Active implements State {

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
