package com.example.sumayerestaurant.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.sumayerestaurant.data.api.ApiService;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.model.RestaurantTable;
import com.example.sumayerestaurant.util.ConnectivityUtil;
import com.example.sumayerestaurant.util.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class TableRepository {
    private final ApiService apiService;
    private final Context context;

    public TableRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getApiService(this.context);
    }

    public interface TableCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void getTables(Long branchId, TableCallback<List<RestaurantTable>> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.getTables(branchId).enqueue(new Callback<List<RestaurantTable>>() {
            @Override
            public void onResponse(Call<List<RestaurantTable>> call, Response<List<RestaurantTable>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String serverMessage = null;
                    if (response.errorBody() != null) {
                        try {
                            serverMessage = response.errorBody().string();
                        } catch (Exception ignored) {}
                    }
                    android.util.Log.e("TableRepository", "getTables failed: code=" + response.code() + ", body=" + serverMessage);
                    callback.onError("Hitilafu katika kupata orodha ya meza (HTTP " + response.code() + ").");
                }
            }

            @Override
            public void onFailure(Call<List<RestaurantTable>> call, Throwable t) {
                android.util.Log.e("TableRepository", "getTables error: " + t.getMessage(), t);
                callback.onError("Imeshindwa kuwasiliana na seva: " + t.getMessage());
            }
        });
    }
}
