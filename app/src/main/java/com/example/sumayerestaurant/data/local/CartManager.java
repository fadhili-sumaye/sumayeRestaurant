package com.example.sumayerestaurant.data.local;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.sumayerestaurant.data.model.CartItem;
import com.example.sumayerestaurant.data.model.MenuItem;
import com.example.sumayerestaurant.data.model.OrderItemRequest;
import com.example.sumayerestaurant.data.model.RestaurantTable;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();
    private RestaurantTable selectedTable;
    private final MutableLiveData<List<CartItem>> cartLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> itemCountLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Double> subtotalLiveData = new MutableLiveData<>(0.0);

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public RestaurantTable getSelectedTable() {
        return selectedTable;
    }

    public void setSelectedTable(RestaurantTable table) {
        this.selectedTable = table;
    }

    public synchronized void addItem(MenuItem menuItem, int quantity, String specialInstructions) {
        if (menuItem == null || quantity <= 0) return;

        for (CartItem item : cartItems) {
            if (item.getMenuItem().getId().equals(menuItem.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                if (specialInstructions != null && !specialInstructions.trim().isEmpty()) {
                    item.setSpecialInstructions(specialInstructions);
                }
                notifyChanges();
                return;
            }
        }

        cartItems.add(new CartItem(menuItem, quantity, specialInstructions));
        notifyChanges();
    }

    public synchronized void updateQuantity(Long menuItemId, int newQuantity) {
        if (menuItemId == null) return;

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getMenuItem().getId().equals(menuItemId)) {
                if (newQuantity <= 0) {
                    cartItems.remove(i);
                } else {
                    item.setQuantity(newQuantity);
                }
                notifyChanges();
                return;
            }
        }
    }

    public synchronized void updateSpecialInstructions(Long menuItemId, String instructions) {
        if (menuItemId == null) return;
        for (CartItem item : cartItems) {
            if (item.getMenuItem().getId().equals(menuItemId)) {
                item.setSpecialInstructions(instructions);
                notifyChanges();
                return;
            }
        }
    }

    public synchronized void removeItem(Long menuItemId) {
        if (menuItemId == null) return;
        cartItems.removeIf(item -> item.getMenuItem().getId().equals(menuItemId));
        notifyChanges();
    }

    public synchronized void clearCart() {
        cartItems.clear();
        selectedTable = null;
        notifyChanges();
    }

    public synchronized List<CartItem> getItems() {
        return new ArrayList<>(cartItems);
    }

    public synchronized int getItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    public synchronized double getSubtotal() {
        double subtotal = 0.0;
        for (CartItem item : cartItems) {
            subtotal += item.getSubtotal();
        }
        return subtotal;
    }

    public synchronized List<OrderItemRequest> toOrderItemRequests() {
        List<OrderItemRequest> requests = new ArrayList<>();
        for (CartItem item : cartItems) {
            requests.add(new OrderItemRequest(
                    item.getMenuItem().getId(),
                    item.getQuantity(),
                    item.getSpecialInstructions()
            ));
        }
        return requests;
    }

    public LiveData<List<CartItem>> getCartLiveData() {
        return cartLiveData;
    }

    public LiveData<Integer> getItemCountLiveData() {
        return itemCountLiveData;
    }

    public LiveData<Double> getSubtotalLiveData() {
        return subtotalLiveData;
    }

    private void notifyChanges() {
        cartLiveData.postValue(new ArrayList<>(cartItems));
        itemCountLiveData.postValue(getItemCount());
        subtotalLiveData.postValue(getSubtotal());
    }
}
