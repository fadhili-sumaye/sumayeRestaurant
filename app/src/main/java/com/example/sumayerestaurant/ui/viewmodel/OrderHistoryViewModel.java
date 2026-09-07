package com.example.sumayerestaurant.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.sumayerestaurant.data.model.CancelOrderRequest;
import com.example.sumayerestaurant.data.model.Order;
import com.example.sumayerestaurant.data.repository.OrderRepository;

import java.util.List;

public class OrderHistoryViewModel extends AndroidViewModel {
    private final OrderRepository orderRepository;

    private final MutableLiveData<List<Order>> ordersLiveData = new MutableLiveData<>();
    private final MutableLiveData<Order> orderDetailLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Order> cancelSuccessLiveData = new MutableLiveData<>();

    public OrderHistoryViewModel(@NonNull Application application) {
        super(application);
        this.orderRepository = new OrderRepository(application);
    }

    public LiveData<List<Order>> getOrders() {
        return ordersLiveData;
    }

    public LiveData<Order> getOrderDetail() {
        return orderDetailLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public LiveData<Order> getCancelSuccess() {
        return cancelSuccessLiveData;
    }

    public void loadMyOrders() {
        isLoadingLiveData.setValue(true);
        errorMessageLiveData.setValue(null);

        orderRepository.getMyOrders(new OrderRepository.OrderCallback<>() {
            @Override
            public void onSuccess(List<Order> result) {
                isLoadingLiveData.postValue(false);
                ordersLiveData.postValue(result);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.postValue(false);
                errorMessageLiveData.postValue(message);
            }
        });
    }

    public void loadOrderDetail(Long orderId) {
        isLoadingLiveData.setValue(true);
        errorMessageLiveData.setValue(null);

        orderRepository.getOrderById(orderId, new OrderRepository.OrderCallback<>() {
            @Override
            public void onSuccess(Order result) {
                isLoadingLiveData.postValue(false);
                orderDetailLiveData.postValue(result);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.postValue(false);
                errorMessageLiveData.postValue(message);
            }
        });
    }

    public void cancelOrder(Long orderId, String reason) {
        isLoadingLiveData.setValue(true);
        errorMessageLiveData.setValue(null);

        CancelOrderRequest request = new CancelOrderRequest(reason);
        orderRepository.cancelOrder(orderId, request, new OrderRepository.OrderCallback<>() {
            @Override
            public void onSuccess(Order result) {
                isLoadingLiveData.postValue(false);
                cancelSuccessLiveData.postValue(result);
                loadMyOrders(); // Refresh list
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.postValue(false);
                errorMessageLiveData.postValue(message);
            }
        });
    }
}
