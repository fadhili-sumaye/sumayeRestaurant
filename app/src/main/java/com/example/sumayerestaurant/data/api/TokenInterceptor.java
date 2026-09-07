package com.example.sumayerestaurant.data.api;

import android.content.Context;
import com.example.sumayerestaurant.data.local.TokenManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class TokenInterceptor implements Interceptor {
    private Context context;

    public TokenInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Get token from TokenManager
        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken();
        
        // If token exists, add it to the request header
        Request newRequest = originalRequest;
        if (token != null) {
            newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
        }
        
        return chain.proceed(newRequest);
    }
}
