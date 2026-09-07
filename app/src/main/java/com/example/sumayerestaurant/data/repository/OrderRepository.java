package com.example.sumayerestaurant.data.repository;

import android.content.Context;
import com.example.sumayerestaurant.data.api.ApiService;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.model.CancelOrderRequest;
import com.example.sumayerestaurant.data.model.CreateOrderRequest;
import com.example.sumayerestaurant.data.model.Order;
import com.example.sumayerestaurant.util.ConnectivityUtil;
import com.example.sumayerestaurant.util.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class OrderRepository {
    private final ApiService apiService;
    private final Context context;

    public OrderRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getApiService(this.context);
    }

    public interface OrderCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void createOrder(CreateOrderRequest request, OrderCallback<Order> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.createOrder(request).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = parseErrorMessage(response);
                    callback.onError(errorMsg != null ? errorMsg : "Hitilafu katika kutuma oda.");
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                callback.onError("Imeshindwa kutuma oda: " + t.getMessage());
            }
        });
    }

    public void getMyOrders(OrderCallback<List<Order>> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.getMyOrders().enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = parseErrorMessage(response);
                    callback.onError(errorMsg != null ? errorMsg : "Hitilafu katika kupata orodha ya oda.");
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                callback.onError("Imeshindwa kupata oda: " + t.getMessage());
            }
        });
    }

    public void getOrderById(Long id, OrderCallback<Order> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.getOrderById(id).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = parseErrorMessage(response);
                    callback.onError(errorMsg != null ? errorMsg : "Hitilafu katika kupata maelezo ya oda.");
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                callback.onError("Imeshindwa kuunganishwa na seva: " + t.getMessage());
            }
        });
    }

    public void cancelOrder(Long id, CancelOrderRequest request, OrderCallback<Order> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.cancelOrder(id, request).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = parseErrorMessage(response);
                    callback.onError(errorMsg != null ? errorMsg : "Hitilafu katika kughairi oda.");
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                callback.onError("Imeshindwa kughairi oda: " + t.getMessage());
            }
        });
    }

    private String parseErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                JsonObject obj = new Gson().fromJson(errorJson, JsonObject.class);
                if (obj.has("message")) {
                    return obj.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
