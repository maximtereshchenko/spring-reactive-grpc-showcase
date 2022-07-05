package com.github.xini1.usecase;

import com.github.xini1.User;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewCartUseCase {

    CartView view(UUID userId, User regular);

    final class CartView {
        private final Set<ItemView> itemViews;

        public CartView(Set<ItemView> itemViews) {
            this.itemViews = Set.copyOf(itemViews);
        }

        public CartView() {
            this(Set.of());
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemViews);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            var cartView = (CartView) object;
            return Objects.equals(itemViews, cartView.itemViews);
        }

        @Override
        public String toString() {
            return "CartView{" +
                    "itemViews=" + itemViews +
                    '}';
        }
    }

    final class ItemView {
        private final UUID itemId;
        private final String itemName;
        private final int quantity;

        public ItemView(UUID itemId, String itemName, int quantity) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantity = quantity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemId, itemName, quantity);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            var itemView = (ItemView) object;
            return quantity == itemView.quantity &&
                    Objects.equals(itemId, itemView.itemId) &&
                    Objects.equals(itemName, itemView.itemName);
        }

        @Override
        public String toString() {
            return "ItemView{" +
                    "itemId=" + itemId +
                    ", itemName='" + itemName + '\'' +
                    ", quantity=" + quantity +
                    '}';
        }
    }
}
