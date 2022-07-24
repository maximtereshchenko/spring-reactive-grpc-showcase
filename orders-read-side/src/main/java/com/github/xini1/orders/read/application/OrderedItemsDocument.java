package com.github.xini1.orders.read.application;

import com.github.xini1.orders.read.view.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

/**
 * @author Maxim Tereshchenko
 */
@Document(collection = "orderedItems")
final class OrderedItemsDocument {

    @Id
    private UUID userId;
    private List<OrderDocument> orders = List.of();

    OrderedItemsDocument() {
    }

    public OrderedItemsDocument(OrderedItems orderedItems) {
        this.userId = orderedItems.getUserId();
        this.orders = orderedItems.getOrders()
                .stream()
                .map(OrderDocument::new)
                .collect(Collectors.collectingAndThen(Collectors.toList(), List::copyOf));
    }

    OrderedItems toOrderedItems() {
        return new OrderedItems(
                userId,
                orders.stream()
                        .map(OrderDocument::toOrder)
                        .collect(Collectors.toList())
        );
    }

    UUID getUserId() {
        return userId;
    }

    void setUserId(UUID userId) {
        this.userId = userId;
    }

    List<OrderDocument> getOrders() {
        return orders;
    }

    void setOrders(List<OrderDocument> orders) {
        this.orders = List.copyOf(orders);
    }

    static final class OrderDocument {

        private Instant timestamp;
        private Collection<ItemInOrderDocument> items = List.of();

        OrderDocument() {
        }

        public OrderDocument(OrderedItems.Order order) {
            this.timestamp = order.getTimestamp();
            this.items = order.getItems()
                    .stream()
                    .map(ItemInOrderDocument::new)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), List::copyOf));
        }

        Instant getTimestamp() {
            return timestamp;
        }

        void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        Collection<ItemInOrderDocument> getItems() {
            return items;
        }

        void setItems(Collection<ItemInOrderDocument> items) {
            this.items = List.copyOf(items);
        }

        private OrderedItems.Order toOrder() {
            return new OrderedItems.Order(
                    timestamp,
                    items.stream()
                            .map(ItemInOrderDocument::toItemInOrder)
                            .collect(Collectors.toList())
            );
        }
    }

    static final class ItemInOrderDocument {

        private UUID id;
        private int quantity;

        ItemInOrderDocument() {
        }

        public ItemInOrderDocument(OrderedItems.ItemInOrder itemInOrder) {
            this.id = itemInOrder.getId();
            this.quantity = itemInOrder.getQuantity();
        }

        UUID getId() {
            return id;
        }

        void setId(UUID id) {
            this.id = id;
        }

        int getQuantity() {
            return quantity;
        }

        void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        private OrderedItems.ItemInOrder toItemInOrder() {
            return new OrderedItems.ItemInOrder(id, quantity);
        }
    }
}
