package com.github.xini1.orders.read.application;

import com.github.xini1.orders.read.view.Cart;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Maxim Tereshchenko
 */
@Document(collection = "carts")
final class CartDocument {

    @Id
    private UUID userId;
    private Collection<ItemInCartDocument> itemsInCart = List.of();
    private long version;

    CartDocument() {
    }

    public CartDocument(Cart cart) {
        this.userId = cart.getUserId();
        this.itemsInCart = cart.getItemsInCart()
                .stream()
                .map(ItemInCartDocument::new)
                .collect(Collectors.collectingAndThen(Collectors.toList(), List::copyOf));
        this.version = cart.getVersion();
    }

    Cart toCart() {
        return new Cart(
                userId,
                itemsInCart.stream()
                        .map(ItemInCartDocument::toItemInCart)
                        .collect(Collectors.toList()),
                version
        );
    }

    UUID getUserId() {
        return userId;
    }

    void setUserId(UUID userId) {
        this.userId = userId;
    }

    Collection<ItemInCartDocument> getItemsInCart() {
        return itemsInCart;
    }

    void setItemsInCart(Collection<ItemInCartDocument> itemsInCart) {
        this.itemsInCart = List.copyOf(itemsInCart);
    }

    long getVersion() {
        return version;
    }

    void setVersion(long version) {
        this.version = version;
    }

    static final class ItemInCartDocument {

        private UUID id;
        private String name;
        private boolean active;
        private int quantity;
        private long version;

        ItemInCartDocument() {
        }

        public ItemInCartDocument(Cart.ItemInCart itemInCart) {
            this.id = itemInCart.getId();
            this.name = itemInCart.getName();
            this.active = itemInCart.isActive();
            this.quantity = itemInCart.getQuantity();
            this.version = itemInCart.getVersion();
        }

        Cart.ItemInCart toItemInCart() {
            return new Cart.ItemInCart(id, name, active, quantity, version);
        }

        UUID getId() {
            return id;
        }

        void setId(UUID id) {
            this.id = id;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        boolean isActive() {
            return active;
        }

        void setActive(boolean active) {
            this.active = active;
        }

        int getQuantity() {
            return quantity;
        }

        void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        long getVersion() {
            return version;
        }

        void setVersion(long version) {
            this.version = version;
        }
    }
}
