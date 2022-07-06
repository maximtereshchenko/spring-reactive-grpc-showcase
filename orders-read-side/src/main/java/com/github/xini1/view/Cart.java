package com.github.xini1.view;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public final class Cart {

    private final UUID userId;
    private final Set<ItemInCart> itemsInCart;

    public Cart(UUID userId, Set<ItemInCart> itemsInCart) {
        this.userId = userId;
        this.itemsInCart = Set.copyOf(itemsInCart);
    }

    public Cart(UUID userId, ItemInCart... itemsInCart) {
        this(userId, Set.of(itemsInCart));
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, itemsInCart);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var cart = (Cart) object;
        return Objects.equals(userId, cart.userId) &&
                Objects.equals(itemsInCart, cart.itemsInCart);
    }

    @Override
    public String toString() {
        return "Cart{" +
                "userId=" + userId +
                ", itemsInCart=" + itemsInCart +
                '}';
    }

    public Cart with(Item item, int quantity) {
        var itemInCart = new ItemInCart(item, quantity);
        var copy = new ArrayList<>(itemsInCart);
        var index = copy.indexOf(itemInCart);

        if (index == -1) {
            copy.add(itemInCart);
        } else {
            copy.set(index, itemInCart.addQuantity(copy.get(index).quantity));
        }

        return new Cart(userId, Set.copyOf(copy));
    }

    public Cart without(Item item, int quantity) {
        var itemInCart = new ItemInCart(item);
        var copy = new ArrayList<>(itemsInCart);
        var index = copy.indexOf(itemInCart);
        var present = copy.get(index);

        if (present.quantity == quantity) {
            copy.remove(present);
        } else {
            copy.set(index, present.removeQuantity(quantity));
        }

        return new Cart(userId, Set.copyOf(copy));
    }

    public static final class ItemInCart {

        private final UUID id;
        private final String name;
        private final boolean active;
        private final int quantity;

        public ItemInCart(UUID id, String name, boolean active, int quantity) {
            this.id = id;
            this.name = name;
            this.active = active;
            this.quantity = quantity;
        }

        private ItemInCart(Item item, int quantity) {
            this(item.getId(), item.getName(), item.isActive(), quantity);
        }

        private ItemInCart(Item item) {
            this(item, 0);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, active);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            var that = (ItemInCart) object;
            return active == that.active &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(name, that.name);
        }

        @Override
        public String toString() {
            return "ItemInCart{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", active=" + active +
                    ", quantity=" + quantity +
                    '}';
        }

        private ItemInCart removeQuantity(int quantity) {
            return addQuantity(-quantity);
        }

        private ItemInCart addQuantity(int quantity) {
            return new ItemInCart(id, name, active, this.quantity + quantity);
        }
    }
}
