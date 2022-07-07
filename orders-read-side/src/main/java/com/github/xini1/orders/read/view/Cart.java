package com.github.xini1.orders.read.view;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class Cart {

    private final UUID userId;
    private final Collection<ItemInCart> itemsInCart;
    private final long version;

    public Cart(UUID userId, Collection<ItemInCart> itemsInCart, long version) {
        this.userId = userId;
        this.itemsInCart = List.copyOf(itemsInCart);
        this.version = version;
    }

    public Cart(UUID userId, long version, ItemInCart... itemsInCart) {
        this(userId, List.of(itemsInCart), version);
    }

    public Cart(UUID userId) {
        this(userId, 0);
    }

    public UUID getUserId() {
        return userId;
    }

    public Collection<ItemInCart> getItemsInCart() {
        return itemsInCart;
    }

    public long getVersion() {
        return version;
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
        var cart = (Cart) object;
        return version == cart.version &&
                Objects.equals(userId, cart.userId) &&
                Objects.equals(itemsInCart, cart.itemsInCart);
    }

    @Override
    public String toString() {
        return "Cart{" +
                "userId=" + userId +
                ", itemsInCart=" + itemsInCart +
                ", version=" + version +
                '}';
    }

    public Cart with(Item item, int quantity, long version) {
        var copy = new ArrayList<>(itemsInCart);
        var index = indexOf(copy, item);

        if (index == -1) {
            copy.add(new ItemInCart(item, quantity));
        } else {
            copy.set(index, copy.get(index).addQuantity(quantity));
        }

        return new Cart(userId, Set.copyOf(copy), version);
    }

    public Cart without(Item item, int quantity, long version) {
        var copy = new ArrayList<>(itemsInCart);
        var index = indexOf(copy, item);
        var present = copy.get(index);

        if (present.getQuantity() == quantity) {
            copy.remove(present);
        } else {
            copy.set(index, present.removeQuantity(quantity));
        }

        return new Cart(userId, Set.copyOf(copy), version);
    }

    public Cart withDeactivated(Item deactivatedItem) {
        var copy = new ArrayList<>(itemsInCart);
        var index = indexOf(copy, deactivatedItem);
        copy.set(index, copy.get(index).deactivated(deactivatedItem.getVersion()));
        return new Cart(userId, Set.copyOf(copy), version);
    }

    public Cart withActivated(Item activatedItem) {
        var copy = new ArrayList<>(itemsInCart);
        var index = indexOf(copy, activatedItem);
        copy.set(index, copy.get(index).activated(activatedItem.getVersion()));
        return new Cart(userId, Set.copyOf(copy), version);
    }

    public boolean hasVersionGreaterOrEqualTo(long version) {
        return this.version >= version;
    }

    private int indexOf(List<ItemInCart> items, Item item) {
        for (var i = 0; i < items.size(); i++) {
            var itemInCart = items.get(i);
            if (itemInCart.getId().equals(item.getId()) && itemInCart.getName().equals(item.getName())) {
                return i;
            }
        }
        return -1;
    }

    public static final class ItemInCart {

        private final UUID id;
        private final String name;
        private final boolean active;
        private final int quantity;
        private final long version;

        public ItemInCart(UUID id, String name, boolean active, int quantity, long version) {
            this.id = id;
            this.name = name;
            this.active = active;
            this.quantity = quantity;
            this.version = version;
        }

        private ItemInCart(Item item, int quantity) {
            this(item.getId(), item.getName(), item.isActive(), quantity, item.getVersion());
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean isActive() {
            return active;
        }

        public int getQuantity() {
            return quantity;
        }

        public boolean hasVersionLessThan(long version) {
            return this.version < version;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, active, quantity);
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
                    quantity == that.quantity &&
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

        private ItemInCart activated(long version) {
            return new ItemInCart(id, name, true, quantity, version);
        }

        private ItemInCart deactivated(long version) {
            return new ItemInCart(id, name, false, quantity, version);
        }

        private ItemInCart removeQuantity(int quantity) {
            return addQuantity(-quantity);
        }

        private ItemInCart addQuantity(int quantity) {
            return new ItemInCart(id, name, active, this.quantity + quantity, version);
        }
    }
}
