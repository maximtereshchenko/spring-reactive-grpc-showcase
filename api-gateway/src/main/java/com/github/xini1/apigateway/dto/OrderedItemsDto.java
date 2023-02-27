package com.github.xini1.apigateway.dto;

import com.github.xini1.orders.read.rpc.ItemInOrderMessage;
import com.github.xini1.orders.read.rpc.OrderMessage;
import com.github.xini1.orders.read.rpc.OrderedItemsResponse;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Maxim Tereshchenko
 */
public final class OrderedItemsDto {

    private String userId;
    private List<OrderDto> orders = List.of();

    public OrderedItemsDto() {
    }

    public OrderedItemsDto(OrderedItemsResponse orderedItemsResponse) {
        this.userId = orderedItemsResponse.getUserId();
        this.orders = orderedItemsResponse.getOrdersList()
                .stream()
                .map(OrderDto::new)
                .collect(Collectors.collectingAndThen(Collectors.toList(), List::copyOf));
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderDto> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderDto> orders) {
        this.orders = List.copyOf(orders);
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
        var that = (OrderedItemsDto) object;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(orders, that.orders);
    }

    @Override
    public String toString() {
        return "OrderedItemsDto{" +
                "userId='" + userId + '\'' +
                ", orders=" + orders +
                '}';
    }

    public static final class OrderDto {

        private String timestamp;
        private Collection<ItemInOrderDto> items = List.of();

        public OrderDto() {
        }

        public OrderDto(OrderMessage orderMessage) {
            this.timestamp = orderMessage.getTimestamp();
            this.items = orderMessage.getItemsList()
                    .stream()
                    .map(ItemInOrderDto::new)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), List::copyOf));
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public Collection<ItemInOrderDto> getItems() {
            return items;
        }

        public void setItems(Collection<ItemInOrderDto> items) {
            this.items = List.copyOf(items);
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
            var orderDto = (OrderDto) object;
            return Objects.equals(timestamp, orderDto.timestamp) &&
                    Objects.equals(items, orderDto.items);
        }

        @Override
        public String toString() {
            return "OrderDto{" +
                    "timestamp='" + timestamp + '\'' +
                    ", items=" + items +
                    '}';
        }
    }

    public static final class ItemInOrderDto {

        private String id;
        private int quantity;

        public ItemInOrderDto() {
        }

        public ItemInOrderDto(ItemInOrderMessage itemInOrderMessage) {
            this.id = itemInOrderMessage.getId();
            this.quantity = itemInOrderMessage.getQuantity();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
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
            var that = (ItemInOrderDto) object;
            return quantity == that.quantity &&
                    Objects.equals(id, that.id);
        }

        @Override
        public String toString() {
            return "ItemInOrderDto{" +
                    "id='" + id + '\'' +
                    ", quantity=" + quantity +
                    '}';
        }
    }
}
