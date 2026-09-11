package com.example.sumayerestaurant.data.api;

import android.content.Context;
import com.example.sumayerestaurant.BuildConfig;
import com.example.sumayerestaurant.util.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static ApiService getApiService(Context context) {
        if (retrofit == null) {
            retrofit = createRetrofit(context);
        }
        if (apiService == null) {
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    private static Retrofit createRetrofit(Context context) {
        // Log request bodies only in emulator/LAN development, not in production
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        if (!BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        }

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new TokenInterceptor(context))
                .addInterceptor(new RetryInterceptor())
                .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // Reset retrofit and apiService (used on logout)
    public static void reset() {
        retrofit = null;
        apiService = null;
    }
}
