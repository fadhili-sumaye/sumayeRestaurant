package com.example.sumayerestaurant.data.repository;

import android.content.Context;
import com.example.sumayerestaurant.data.api.ApiService;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.model.CancelOrderRequest;
import com.example.sumayerestaurant.data.model.KitchenOrder;
import com.example.sumayerestaurant.util.ConnectivityUtil;
import com.example.sumayerestaurant.util.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class KitchenRepository {
    private final ApiService apiService;
    private final Context context;

    public KitchenRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getApiService(this.context);
    }

    public interface KitchenCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void getActiveOrders(KitchenCallback<List<KitchenOrder>> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.getActiveKitchenOrders().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<KitchenOrder>> call, Response<List<KitchenOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kupata oda za jikoni."));
                }
            }

            @Override
            public void onFailure(Call<List<KitchenOrder>> call, Throwable t) {
                callback.onError("Imeshindwa kuwasiliana na seva: " + t.getMessage());
            }
        });
    }

    public void getKitchenOrderById(Long id, KitchenCallback<KitchenOrder> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.getKitchenOrderById(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<KitchenOrder> call, Response<KitchenOrder> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kupata maelezo ya KOT."));
                }
            }

            @Override
            public void onFailure(Call<KitchenOrder> call, Throwable t) {
                callback.onError("Imeshindwa kuwasiliana na seva: " + t.getMessage());
            }
        });
    }

    public void acceptOrder(Long id, KitchenCallback<KitchenOrder> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.acceptKitchenOrder(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<KitchenOrder> call, Response<KitchenOrder> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kupokea oda."));
                }
            }

            @Override
            public void onFailure(Call<KitchenOrder> call, Throwable t) {
                callback.onError("Imeshindwa kupokea oda: " + t.getMessage());
            }
        });
    }

    public void startPreparingOrder(Long id, KitchenCallback<KitchenOrder> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.startPreparingKitchenOrder(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<KitchenOrder> call, Response<KitchenOrder> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kuanza kuandaa oda."));
                }
            }

            @Override
            public void onFailure(Call<KitchenOrder> call, Throwable t) {
                callback.onError("Imeshindwa kuanza maandalizi: " + t.getMessage());
            }
        });
    }

    public void markOrderReady(Long id, KitchenCallback<KitchenOrder> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.markKitchenOrderReady(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<KitchenOrder> call, Response<KitchenOrder> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kuweka oda tayari."));
                }
            }

            @Override
            public void onFailure(Call<KitchenOrder> call, Throwable t) {
                callback.onError("Imeshindwa kuweka oda tayari: " + t.getMessage());
            }
        });
    }

    public void cancelOrder(Long id, String reason, KitchenCallback<KitchenOrder> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        CancelOrderRequest request = new CancelOrderRequest(reason);
        apiService.cancelKitchenOrder(id, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<KitchenOrder> call, Response<KitchenOrder> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kughairi oda."));
                }
            }

            @Override
            public void onFailure(Call<KitchenOrder> call, Throwable t) {
                callback.onError("Imeshindwa kughairi oda: " + t.getMessage());
            }
        });
    }

    private String parseErrorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                JsonObject obj = new Gson().fromJson(errorJson, JsonObject.class);
                if (obj.has("message")) {
                    return obj.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {}
        return fallback;
    }
}
