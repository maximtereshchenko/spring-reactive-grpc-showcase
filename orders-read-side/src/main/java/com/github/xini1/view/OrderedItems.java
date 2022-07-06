package com.github.xini1.view;

import java.time.*;
import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class OrderedItems {

    private final UUID userId;
    private final List<Order> orders;

    public OrderedItems(UUID userId, List<Order> orders) {
        this.userId = userId;
        this.orders = List.copyOf(orders);
    }

    public OrderedItems(UUID userId, Order... orders) {
        this(userId, List.of(orders));
    }

    public UUID getUserId() {
        return userId;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public OrderedItems withOrder(Order order) {
        var copy = new ArrayList<>(orders);
        copy.add(order);
        return new OrderedItems(userId, copy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, orders);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (OrderedItems) object;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(orders, that.orders);
    }

    @Override
    public String toString() {
        return "OrderedItems{" +
                "userId=" + userId +
                ", orders=" + orders +
                '}';
    }

    public static final class Order {

        private final Instant timestamp;
        private final Collection<ItemInOrder> items;

        public Order(Instant timestamp, Collection<ItemInOrder> items) {
            this.timestamp = timestamp;
            this.items = List.copyOf(items);
        }

        public Order(Instant timestamp, ItemInOrder... items) {
            this(timestamp, List.of(items));
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, items);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            var order = (Order) object;
            return Objects.equals(timestamp, order.timestamp) &&
                    Objects.equals(items, order.items);
        }

        @Override
        public String toString() {
            return "Order{" +
                    "timestamp=" + timestamp +
                    ", items=" + items +
                    '}';
        }
    }

    public static final class ItemInOrder {

        private final UUID id;
        private final int quantity;

        public ItemInOrder(UUID id, int quantity) {
            this.id = id;
            this.quantity = quantity;
        }

        public ItemInOrder(Cart.ItemInCart itemInCart) {
            this(itemInCart.getId(), itemInCart.getQuantity());
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, quantity);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            var that = (ItemInOrder) object;
            return quantity == that.quantity &&
                    Objects.equals(id, that.id);
        }

        @Override
        public String toString() {
            return "ItemInOrder{" +
                    "itemId=" + id +
                    ", quantity=" + quantity +
                    '}';
        }
    }
}
