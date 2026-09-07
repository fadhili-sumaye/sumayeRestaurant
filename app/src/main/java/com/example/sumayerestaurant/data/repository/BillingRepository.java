package com.example.sumayerestaurant.data.repository;

import android.content.Context;
import com.example.sumayerestaurant.data.api.ApiService;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.model.ApplyDiscountRequest;
import com.example.sumayerestaurant.data.model.Bill;
import com.example.sumayerestaurant.data.model.CreatePaymentRequest;
import com.example.sumayerestaurant.data.model.Receipt;
import com.example.sumayerestaurant.util.ConnectivityUtil;
import com.example.sumayerestaurant.util.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class BillingRepository {
    private final ApiService apiService;
    private final Context context;

    public BillingRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getApiService(this.context);
    }

    public interface BillingCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void getActiveBills(BillingCallback<List<Bill>> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.getActiveBills().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Bill>> call, Response<List<Bill>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kupata ankara."));
                }
            }

            @Override
            public void onFailure(Call<List<Bill>> call, Throwable t) {
                callback.onError("Imeshindwa kuwasiliana na seva: " + t.getMessage());
            }
        });
    }

    public void getBillById(Long id, BillingCallback<Bill> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.getBillById(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bill> call, Response<Bill> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kupata ankara."));
                }
            }

            @Override
            public void onFailure(Call<Bill> call, Throwable t) {
                callback.onError("Imeshindwa kuwasiliana na seva: " + t.getMessage());
            }
        });
    }

    public void getBillByOrderId(Long orderId, BillingCallback<Bill> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.getBillByOrderId(orderId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bill> call, Response<Bill> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kupata ankara ya oda."));
                }
            }

            @Override
            public void onFailure(Call<Bill> call, Throwable t) {
                callback.onError("Imeshindwa kuwasiliana na seva: " + t.getMessage());
            }
        });
    }

    public void requestBill(Long orderId, BillingCallback<Bill> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.requestBill(orderId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bill> call, Response<Bill> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kuomba ankara."));
                }
            }

            @Override
            public void onFailure(Call<Bill> call, Throwable t) {
                callback.onError("Imeshindwa kuomba ankara: " + t.getMessage());
            }
        });
    }

    public void applyDiscount(Long billId, ApplyDiscountRequest request, BillingCallback<Bill> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.applyDiscount(billId, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bill> call, Response<Bill> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kutoa punguzo."));
                }
            }

            @Override
            public void onFailure(Call<Bill> call, Throwable t) {
                callback.onError("Imeshindwa kutoa punguzo: " + t.getMessage());
            }
        });
    }

    public void processPayment(Long billId, CreatePaymentRequest request, BillingCallback<Bill> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.processPayment(billId, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Bill> call, Response<Bill> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kuthibitisha malipo."));
                }
            }

            @Override
            public void onFailure(Call<Bill> call, Throwable t) {
                callback.onError("Imeshindwa kukamilisha malipo: " + t.getMessage());
            }
        });
    }

    public void getReceipt(Long billId, BillingCallback<Receipt> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }

        apiService.getReceipt(billId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Receipt> call, Response<Receipt> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Hitilafu katika kupata risiti."));
                }
            }

            @Override
            public void onFailure(Call<Receipt> call, Throwable t) {
                callback.onError("Imeshindwa kupata risiti: " + t.getMessage());
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
