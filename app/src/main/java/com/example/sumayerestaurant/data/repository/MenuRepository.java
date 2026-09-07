package com.example.sumayerestaurant.data.repository;

import android.content.Context;
import com.example.sumayerestaurant.data.api.ApiService;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.model.MenuCategory;
import com.example.sumayerestaurant.data.model.MenuItem;
import com.example.sumayerestaurant.util.ConnectivityUtil;
import com.example.sumayerestaurant.util.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class MenuRepository {
    private final ApiService apiService;
    private final Context context;

    public MenuRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getApiService(this.context);
    }

    public interface MenuCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void getCategories(Long branchId, MenuCallback<List<MenuCategory>> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.getCategories(branchId).enqueue(new Callback<List<MenuCategory>>() {
            @Override
            public void onResponse(Call<List<MenuCategory>> call, Response<List<MenuCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Hitilafu katika kupata aina za vyakula.");
                }
            }

            @Override
            public void onFailure(Call<List<MenuCategory>> call, Throwable t) {
                callback.onError("Imeshindwa kuunganishwa na seva: " + t.getMessage());
            }
        });
    }

    public void getMenuItems(Long branchId, MenuCallback<List<MenuItem>> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.getMenuItems(branchId).enqueue(new Callback<List<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Hitilafu katika kupata orodha ya vyakula.");
                }
            }

            @Override
            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                callback.onError("Imeshindwa kuunganishwa na seva: " + t.getMessage());
            }
        });
    }
}
