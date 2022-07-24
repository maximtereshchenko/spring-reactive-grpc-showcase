package com.github.xini1.apigateway.dto;

import com.github.xini1.orders.read.rpc.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Maxim Tereshchenko
 */
public final class CartDto {

    private String userId;
    private Collection<ItemInCartDto> itemsInCart = List.of();
    private long version;

    public CartDto() {
    }

    public CartDto(CartResponse cartResponse) {
        this.userId = cartResponse.getUserId();
        this.itemsInCart = cartResponse.getItemsInCartList()
                .stream()
                .map(ItemInCartDto::new)
                .collect(Collectors.collectingAndThen(Collectors.toList(), List::copyOf));
        this.version = cartResponse.getVersion();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Collection<ItemInCartDto> getItemsInCart() {
        return itemsInCart;
    }

    public void setItemsInCart(Collection<ItemInCartDto> itemsInCart) {
        this.itemsInCart = List.copyOf(itemsInCart);
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, itemsInCart, version);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var cartDto = (CartDto) object;
        return version == cartDto.version &&
                Objects.equals(userId, cartDto.userId) &&
                Objects.equals(itemsInCart, cartDto.itemsInCart);
    }

    @Override
    public String toString() {
        return "CartDto{" +
                "userId='" + userId + '\'' +
                ", itemsInCart=" + itemsInCart +
                ", version=" + version +
                '}';
    }

    public static final class ItemInCartDto {

        private String id;
        private String name;
        private boolean active;
        private int quantity;
        private long version;

        public ItemInCartDto() {
        }

        private ItemInCartDto(ItemInCartMessage itemInCartMessage) {
            this.id = itemInCartMessage.getId();
            this.name = itemInCartMessage.getName();
            this.active = itemInCartMessage.getActive();
            this.quantity = itemInCartMessage.getQuantity();
            this.version = itemInCartMessage.getVersion();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public long getVersion() {
            return version;
        }

        public void setVersion(long version) {
            this.version = version;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, active, quantity, version);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            var that = (ItemInCartDto) object;
            return active == that.active &&
                    quantity == that.quantity &&
                    version == that.version &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(name, that.name);
        }

        @Override
        public String toString() {
            return "ItemInCartDto{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", active=" + active +
                    ", quantity=" + quantity +
                    ", version=" + version +
                    '}';
        }
    }
}
