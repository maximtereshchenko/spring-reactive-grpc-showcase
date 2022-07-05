package com.github.xini1.view;

import com.github.xini1.domain.Item;

import java.util.HashSet;
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

    public Set<ItemInCart> getItemsInCart() {
        return itemsInCart;
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
        var found = itemsInCart.stream()
                .filter(itemInCart -> itemInCart.equalTo(item))
                .findAny();
        var copy = new HashSet<>(itemsInCart);
        if (found.isPresent()) {
            var itemInCart = found.get();
            copy.remove(itemInCart);
            copy.add(itemInCart.addQuantity(quantity));
        } else {
            copy.add(new ItemInCart(item, quantity));
        }
        return new Cart(userId, copy);
    }

    public static final class ItemInCart {

        private final UUID id;
        private final String name;
        private final int quantity;

        public ItemInCart(UUID id, String name, int quantity) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
        }

        private ItemInCart(Item item, int quantity) {
            this(item.getId(), item.getName(), quantity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            var itemView = (ItemInCart) object;
            return Objects.equals(id, itemView.id) &&
                    Objects.equals(name, itemView.name);
        }

        @Override
        public String toString() {
            return "Item{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", quantity=" + quantity +
                    '}';
        }

        private ItemInCart addQuantity(int quantity) {
            return new ItemInCart(id, name, this.quantity + quantity);
        }

        private boolean equalTo(Item item) {
            return Objects.equals(id, item.getId()) &&
                    Objects.equals(name, item.getName());
        }
    }
}
