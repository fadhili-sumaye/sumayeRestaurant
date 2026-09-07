package com.example.sumayerestaurant.data.repository;

import android.content.Context;
import com.example.sumayerestaurant.data.api.ApiService;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.model.OwnerDashboard;
import com.example.sumayerestaurant.util.ConnectivityUtil;
import com.example.sumayerestaurant.util.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerRepository {
    private final ApiService apiService;
    private final Context context;

    public OwnerRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getApiService(this.context);
    }

    public interface OwnerCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public interface ExportCallback {
        void onSuccess(File file);
        void onError(String message);
    }

    public void getDashboard(Long branchId, String from, String to, String groupBy, OwnerCallback<OwnerDashboard> callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }
        apiService.getOwnerDashboard(branchId, from, to, groupBy).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<OwnerDashboard> call, Response<OwnerDashboard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response, "Imeshindwa kupakia takwimu za dashibodi."));
                }
            }

            @Override
            public void onFailure(Call<OwnerDashboard> call, Throwable t) {
                callback.onError("Imeshindwa kuwasiliana na seva: " + t.getMessage());
            }
        });
    }

    public void exportReport(Long branchId, String from, String to, String format, ExportCallback callback) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            callback.onError(Constants.ERROR_NETWORK);
            return;
        }
        apiService.exportOwnerReport(branchId, from, to, format).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        byte[] bytes = response.body().bytes();
                        File dir = new File(context.getExternalFilesDir(null), "reports");
                        if (!dir.exists() && !dir.mkdirs()) throw new Exception("Haiwezi kuunda folda");
                        boolean pdf = "pdf".equalsIgnoreCase(format);
                        String name = "riporti-biashara-"
                                + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date())
                                + (pdf ? ".pdf" : ".csv");
                        File file = new File(dir, name);
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(bytes);
                        }
                        callback.onSuccess(file);
                    } catch (Exception e) {
                        callback.onError("Imeshindwa kuhifadhi faili: " + e.getMessage());
                    }
                } else {
                    callback.onError(parseErrorMessage(response, "Imeshindwa kuwa pakua ripoti."));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onError("Imeshindwa kuwasiliana na seva: " + t.getMessage());
            }
        });
    }

    private String parseErrorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                JsonObject obj = new Gson().fromJson(errorJson, JsonObject.class);
                if (obj != null && obj.has("message")) {
                    return obj.get("message").getAsString();
                }
                if (response.code() == 401) return Constants.ERROR_UNAUTHORIZED;
                if (response.code() == 403) return "Huna idhini ya kuona takwimu hizi.";
            }
        } catch (Exception ignored) {}
        return fallback;
    }
}