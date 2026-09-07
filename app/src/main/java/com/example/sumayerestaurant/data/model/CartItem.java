package com.example.sumayerestaurant.data.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private MenuItem menuItem;
    private int quantity;
    private String specialInstructions;

    public CartItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.specialInstructions = "";
    }

    public CartItem(MenuItem menuItem, int quantity, String specialInstructions) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.specialInstructions = specialInstructions != null ? specialInstructions : "";
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions != null ? specialInstructions : "";
    }

    public double getSubtotal() {
        if (menuItem == null) return 0.0;
        return menuItem.getPrice() * quantity;
    }
}
