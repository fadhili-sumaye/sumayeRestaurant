package com.example.sumayerestaurant;

import android.app.Application;
import com.example.sumayerestaurant.util.Constants;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Wakes the cloud backend as early as possible so that by the time the user
 * interacts (login or dashboard), the Render free tier is no longer cold-starting.
 * Errors are intentionally ignored: this is a background warm-up only.
 */
public class SumayeApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        warmUpBackend();
    }

    private void warmUpBackend() {
        try {
            Thread worker = new Thread(() -> {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(12, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .callTimeout(90, TimeUnit.SECONDS)
                        .build();
                Request request = new Request.Builder()
                        .url(Constants.BASE_URL + "api/auth/health")
                        .build();
                try {
                    okhttp3.Response response = client.newCall(request).execute();
                    response.close();
                } catch (Exception ignored) {
                }
            });
            worker.setDaemon(true);
            worker.start();
        } catch (Exception ignored) {
        }
    }
}