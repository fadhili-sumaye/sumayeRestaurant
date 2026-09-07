package com.example.sumayerestaurant.data.api;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Retries idempotent (GET/HEAD/OPTIONS) requests once when the first attempt
 * fails because the cloud backend was still cold-starting (connect/read timeout
 * or connection refused). Never retries body-bearing methods, so no double side effects.
 */
public class RetryInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        try {
            return chain.proceed(request);
        } catch (IOException e) {
            if (isRetryableMethod(request) && isTransient(e)) {
                try {
                    return chain.proceed(request);
                } catch (IOException second) {
                    throw second;
                }
            }
            throw e;
        }
    }

    private boolean isRetryableMethod(Request request) {
        String method = request.method();
        return "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
    }

    private boolean isTransient(Exception e) {
        return e instanceof SocketTimeoutException || e instanceof ConnectException;
    }
}