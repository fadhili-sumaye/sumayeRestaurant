package com.example.sumayerestaurant.ui.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.*;
import com.example.sumayerestaurant.data.repository.BillingRepository;
import com.example.sumayerestaurant.data.websocket.StompClient;
import com.example.sumayerestaurant.data.websocket.WebSocketManager;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CashierViewModel extends AndroidViewModel {
    private static final String TAG = "CashierViewModel";

    private final BillingRepository billingRepository;
    private final WebSocketManager webSocketManager;
    private final TokenManager tokenManager;
    private final Gson gson = new Gson();

    private final MutableLiveData<List<Bill>> allBillsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Bill>> filteredBillsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<BigDecimal> totalSalesTodayLiveData = new MutableLiveData<>(BigDecimal.ZERO);
    private final MutableLiveData<Integer> unpaidCountLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> paidCountLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<RealTimeEvent> billRequestedEventLiveData = new MutableLiveData<>();

    private String currentFilter = "UNPAID"; // UNPAID, PAID, ALL
    private String searchQuery = "";
    private Long branchId = 1L;

    public CashierViewModel(@NonNull Application application) {
        super(application);
        this.billingRepository = new BillingRepository(application);
        this.webSocketManager = WebSocketManager.getInstance(application);
        this.tokenManager = new TokenManager(application);

        User user = tokenManager.getUser();
        if (user != null && user.getBranchId() != null) {
            this.branchId = user.getBranchId();
        }
    }

    public LiveData<List<Bill>> getFilteredBills() { return filteredBillsLiveData; }
    public LiveData<BigDecimal> getTotalSalesToday() { return totalSalesTodayLiveData; }
    public LiveData<Integer> getUnpaidCount() { return unpaidCountLiveData; }
    public LiveData<Integer> getPaidCount() { return paidCountLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoadingLiveData; }
    public LiveData<String> getErrorMessage() { return errorMessageLiveData; }
    public LiveData<RealTimeEvent> getBillRequestedEvent() { return billRequestedEventLiveData; }
    public LiveData<StompClient.ConnectionState> getConnectionState() { return webSocketManager.getConnectionStateLiveData(); }

    public void initRealTime() {
        webSocketManager.connect();
        String topic = "/topic/branches/" + branchId + "/cashier";

        webSocketManager.subscribe(topic, (destination, payload) -> {
            try {
                Log.d(TAG, "Cashier received real-time event: " + payload);
                RealTimeEvent event = gson.fromJson(payload, RealTimeEvent.class);
                if (event != null && "BILL_REQUESTED".equalsIgnoreCase(event.getEventType())) {
                    billRequestedEventLiveData.postValue(event);
                }
                loadBills();
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse cashier real-time event: " + e.getMessage());
            }
        });

        loadBills();
    }

    public void loadBills() {
        isLoadingLiveData.setValue(true);
        billingRepository.getActiveBills(new BillingRepository.BillingCallback<>() {
            @Override
            public void onSuccess(List<Bill> result) {
                isLoadingLiveData.postValue(false);
                allBillsLiveData.postValue(result);
                calculateMetrics(result);
                applyFilterAndSearch(result);
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
        List<Bill> all = allBillsLiveData.getValue();
        if (all != null) {
            applyFilterAndSearch(all);
        }
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query.trim().toLowerCase() : "";
        List<Bill> all = allBillsLiveData.getValue();
        if (all != null) {
            applyFilterAndSearch(all);
        }
    }

    private void calculateMetrics(List<Bill> bills) {
        BigDecimal totalSales = BigDecimal.ZERO;
        int unpaid = 0;
        int paid = 0;

        for (Bill b : bills) {
            if ("PAID".equalsIgnoreCase(b.getPaymentStatus())) {
                paid++;
                totalSales = totalSales.add(b.getAmountPaid());
            } else if ("UNPAID".equalsIgnoreCase(b.getPaymentStatus()) || "PARTIALLY_PAID".equalsIgnoreCase(b.getPaymentStatus())) {
                unpaid++;
                totalSales = totalSales.add(b.getAmountPaid());
            }
        }

        totalSalesTodayLiveData.postValue(totalSales);
        unpaidCountLiveData.postValue(unpaid);
        paidCountLiveData.postValue(paid);
    }

    private void applyFilterAndSearch(List<Bill> bills) {
        List<Bill> filtered = bills.stream().filter(bill -> {
            boolean matchesFilter = true;
            if ("UNPAID".equalsIgnoreCase(currentFilter)) {
                matchesFilter = "UNPAID".equalsIgnoreCase(bill.getPaymentStatus()) || "PARTIALLY_PAID".equalsIgnoreCase(bill.getPaymentStatus());
            } else if ("PAID".equalsIgnoreCase(currentFilter)) {
                matchesFilter = "PAID".equalsIgnoreCase(bill.getPaymentStatus());
            }

            if (!matchesFilter) return false;

            if (searchQuery.isEmpty()) return true;

            String billNum = bill.getBillNumber() != null ? bill.getBillNumber().toLowerCase() : "";
            String orderNum = bill.getOrderNumber() != null ? bill.getOrderNumber().toLowerCase() : "";
            String tableNum = bill.getTableNumber() != null ? String.valueOf(bill.getTableNumber()) : "";
            String waiter = bill.getWaiterName() != null ? bill.getWaiterName().toLowerCase() : "";

            return billNum.contains(searchQuery) || orderNum.contains(searchQuery) || tableNum.contains(searchQuery) || waiter.contains(searchQuery);
        }).collect(Collectors.toList());

        filteredBillsLiveData.postValue(filtered);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        String topic = "/topic/branches/" + branchId + "/cashier";
        webSocketManager.unsubscribe(topic);
    }
}
