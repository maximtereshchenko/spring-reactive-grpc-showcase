package com.github.xini1.apigateway.dto;

/**
 * @author Maxim Tereshchenko
 */
public final class AddItemToCartDto {

    private String itemId;
    private int quantity;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
