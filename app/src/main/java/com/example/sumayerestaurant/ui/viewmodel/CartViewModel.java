package com.example.sumayerestaurant.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.sumayerestaurant.data.local.CartManager;
import com.example.sumayerestaurant.data.model.CartItem;
import com.example.sumayerestaurant.data.model.CreateOrderRequest;
import com.example.sumayerestaurant.data.model.Order;
import com.example.sumayerestaurant.data.model.RestaurantTable;
import com.example.sumayerestaurant.data.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

public class CartViewModel extends AndroidViewModel {
    private final CartManager cartManager;
    private final OrderRepository orderRepository;

    private final MutableLiveData<Boolean> isSubmittingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Order> orderSuccessLiveData = new MutableLiveData<>();

    public CartViewModel(@NonNull Application application) {
        super(application);
        this.cartManager = CartManager.getInstance();
        this.orderRepository = new OrderRepository(application);
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartManager.getCartLiveData();
    }

    public LiveData<Double> getSubtotal() {
        return cartManager.getSubtotalLiveData();
    }

    public LiveData<Integer> getItemCount() {
        return cartManager.getItemCountLiveData();
    }

    public RestaurantTable getSelectedTable() {
        return cartManager.getSelectedTable();
    }

    public LiveData<Boolean> getIsSubmitting() {
        return isSubmittingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public LiveData<Order> getOrderSuccess() {
        return orderSuccessLiveData;
    }

    public void updateQuantity(Long menuItemId, int quantity) {
        cartManager.updateQuantity(menuItemId, quantity);
    }

    public void updateInstructions(Long menuItemId, String instructions) {
        cartManager.updateSpecialInstructions(menuItemId, instructions);
    }

    public void removeItem(Long menuItemId) {
        cartManager.removeItem(menuItemId);
    }

    public void submitOrder(String notes) {
        if (Boolean.TRUE.equals(isSubmittingLiveData.getValue())) {
            return; // Already submitting - prevent double submission
        }

        List<CartItem> items = cartManager.getItems();
        if (items.isEmpty()) {
            errorMessageLiveData.setValue("Oda yako haina bidhaa.");
            return;
        }

        RestaurantTable table = cartManager.getSelectedTable();
        Long tableId = table != null ? table.getId() : null;

        isSubmittingLiveData.setValue(true);
        errorMessageLiveData.setValue(null);

        // Generate client-side idempotency key
        String clientRequestId = UUID.randomUUID().toString();

        CreateOrderRequest request = new CreateOrderRequest(
                tableId,
                "DINE_IN",
                clientRequestId,
                notes,
                cartManager.toOrderItemRequests()
        );

        orderRepository.createOrder(request, new OrderRepository.OrderCallback<>() {
            @Override
            public void onSuccess(Order result) {
                isSubmittingLiveData.postValue(false);
                cartManager.clearCart();
                orderSuccessLiveData.postValue(result);
            }

            @Override
            public void onError(String message) {
                isSubmittingLiveData.postValue(false);
                errorMessageLiveData.postValue(message);
            }
        });
    }
}
