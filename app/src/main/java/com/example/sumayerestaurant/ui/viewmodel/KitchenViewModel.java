package com.example.sumayerestaurant.ui.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.KitchenOrder;
import com.example.sumayerestaurant.data.model.RealTimeEvent;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.data.repository.KitchenRepository;
import com.example.sumayerestaurant.data.websocket.StompClient;
import com.example.sumayerestaurant.data.websocket.WebSocketManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KitchenViewModel extends AndroidViewModel {
    private static final String TAG = "KitchenViewModel";

    private final KitchenRepository kitchenRepository;
    private final WebSocketManager webSocketManager;
    private final TokenManager tokenManager;
    private final Gson gson = new Gson();

    private final MutableLiveData<List<KitchenOrder>> allOrdersLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<KitchenOrder>> filteredOrdersLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<RealTimeEvent> newEventLiveData = new MutableLiveData<>();

    private String currentFilter = "ALL"; // ALL, NEW, PREPARING, READY
    private boolean isOldestFirst = true;
    private Long branchId;

    public KitchenViewModel(@NonNull Application application) {
        super(application);
        this.kitchenRepository = new KitchenRepository(application);
        this.webSocketManager = WebSocketManager.getInstance(application);
        this.tokenManager = new TokenManager(application);

        User user = tokenManager.getUser();
        if (user != null && user.getBranchId() != null) {
            this.branchId = user.getBranchId();
        } else {
            errorMessageLiveData.setValue("Akaunti hii haina tawi lililowekwa.");
        }
    }

    public LiveData<List<KitchenOrder>> getFilteredOrders() {
        return filteredOrdersLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public LiveData<RealTimeEvent> getNewEvent() {
        return newEventLiveData;
    }

    public LiveData<StompClient.ConnectionState> getConnectionState() {
        return webSocketManager.getConnectionStateLiveData();
    }

    public void initRealTime() {
        webSocketManager.connect();
        String topic = "/topic/branches/" + branchId + "/kitchen";

        webSocketManager.subscribe(topic, (destination, payload) -> {
            try {
                Log.d(TAG, "Received real-time KOT event: " + payload);
                RealTimeEvent event = gson.fromJson(payload, RealTimeEvent.class);
                newEventLiveData.postValue(event);
                // Refresh list upon any event
                loadOrders();
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse real-time event: " + e.getMessage());
            }
        });

        loadOrders();
    }

    public void loadOrders() {
        isLoadingLiveData.setValue(true);
        kitchenRepository.getActiveOrders(new KitchenRepository.KitchenCallback<>() {
            @Override
            public void onSuccess(List<KitchenOrder> result) {
                isLoadingLiveData.postValue(false);
                allOrdersLiveData.postValue(result);
                applyFilterAndSort(result);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.postValue(false);
                errorMessageLiveData.postValue(message);
            }
        });
    }

    public void setFilter(String filter) {
        this.currentFilter = filter;
        List<KitchenOrder> all = allOrdersLiveData.getValue();
        if (all != null) {
            applyFilterAndSort(all);
        }
    }

    public void setSortOldestFirst(boolean oldestFirst) {
        this.isOldestFirst = oldestFirst;
        List<KitchenOrder> all = allOrdersLiveData.getValue();
        if (all != null) {
            applyFilterAndSort(all);
        }
    }

    public boolean isOldestFirst() {
        return isOldestFirst;
    }

    private void applyFilterAndSort(List<KitchenOrder> orders) {
        List<KitchenOrder> filtered = orders.stream().filter(order -> {
            if ("ALL".equalsIgnoreCase(currentFilter)) return true;
            if ("NEW".equalsIgnoreCase(currentFilter)) {
                return "NEW".equalsIgnoreCase(order.getStatus()) || "ACCEPTED".equalsIgnoreCase(order.getStatus());
            }
            if ("PREPARING".equalsIgnoreCase(currentFilter)) {
                return "PREPARING".equalsIgnoreCase(order.getStatus());
            }
            if ("READY".equalsIgnoreCase(currentFilter)) {
                return "READY".equalsIgnoreCase(order.getStatus());
            }
            return true;
        }).collect(Collectors.toList());

        filtered.sort((o1, o2) -> {
            String t1 = o1.getCreatedAt() != null ? o1.getCreatedAt() : "";
            String t2 = o2.getCreatedAt() != null ? o2.getCreatedAt() : "";
            return isOldestFirst ? t1.compareTo(t2) : t2.compareTo(t1);
        });

        filteredOrdersLiveData.postValue(filtered);
    }

    public void acceptOrder(Long id) {
        kitchenRepository.acceptOrder(id, new KitchenRepository.KitchenCallback<>() {
            @Override
            public void onSuccess(KitchenOrder result) {
                loadOrders();
            }

            @Override
            public void onError(String message) {
                errorMessageLiveData.postValue(message);
            }
        });
    }

    public void startPreparingOrder(Long id) {
        kitchenRepository.startPreparingOrder(id, new KitchenRepository.KitchenCallback<>() {
            @Override
            public void onSuccess(KitchenOrder result) {
                loadOrders();
            }

            @Override
            public void onError(String message) {
                errorMessageLiveData.postValue(message);
            }
        });
    }

    public void markOrderReady(Long id) {
        kitchenRepository.markOrderReady(id, new KitchenRepository.KitchenCallback<>() {
            @Override
            public void onSuccess(KitchenOrder result) {
                loadOrders();
            }

            @Override
            public void onError(String message) {
                errorMessageLiveData.postValue(message);
            }
        });
    }

    public void cancelOrder(Long id, String reason) {
        kitchenRepository.cancelOrder(id, reason, new KitchenRepository.KitchenCallback<>() {
            @Override
            public void onSuccess(KitchenOrder result) {
                loadOrders();
            }

            @Override
            public void onError(String message) {
                errorMessageLiveData.postValue(message);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        String topic = "/topic/branches/" + branchId + "/kitchen";
        webSocketManager.unsubscribe(topic);
    }
}
