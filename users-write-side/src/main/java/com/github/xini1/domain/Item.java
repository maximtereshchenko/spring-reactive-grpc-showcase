package com.github.xini1.domain;

import com.github.xini1.exception.ItemHasNotBeenCreated;
import com.github.xini1.usecase.Event;
import com.github.xini1.usecase.EventVisitor;
import com.github.xini1.usecase.Identifiers;
import com.github.xini1.usecase.ItemCreated;
import com.github.xini1.usecase.ItemDeactivated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class Item {

    private final List<Event> newEvents = new ArrayList<>();
    private State state = new Initial();

    static Item create(UUID userId, String name, Identifiers identifiers) {
        var item = new Item();
        new EventHandler(item).visit(new ItemCreated(userId, identifiers.newIdentifier(), name));
        return item;
    }

    static Optional<Item> fromEvents(List<Event> events) {
        if (events.isEmpty()) {
            return Optional.empty();
        }
        var item = new Item();
        var eventHandler = new EventHandler(item);
        events.forEach(event -> event.accept(eventHandler));
        item.newEvents.clear();
        return Optional.of(item);
    }

    List<Event> newEvents() {
        return List.copyOf(newEvents);
    }

    void deactivate(UUID userId) {
        state.deactivate();
        new EventHandler(this).visit(new ItemDeactivated(userId, state.id()));
    }

    private interface State {

        UUID id();

        void activate();

        void deactivate();
    }

    private static final class Initial implements State {

        @Override
        public UUID id() {
            throw new ItemHasNotBeenCreated();
        }

        @Override
        public void activate() {
            throw new ItemHasNotBeenCreated();
        }

        @Override
        public void deactivate() {
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
        public void activate() {
            //TODO
        }

        @Override
        public void deactivate() {
            //empty
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
        public void activate() {
            //TODO
        }

        @Override
        public void deactivate() {
            //TODO
        }
    }

    private static final class EventHandler implements EventVisitor {

        private final Item item;

        EventHandler(Item item) {
            this.item = item;
        }

        @Override
        public void visit(ItemCreated itemCreated) {
            item.state = new Active(itemCreated.itemId());
            item.newEvents.add(itemCreated);
        }

        @Override
        public void visit(ItemDeactivated itemDeactivated) {
            item.state = new Deactivated(itemDeactivated.itemId());
            item.newEvents.add(itemDeactivated);
        }
    }
}
