package com.example.sumayerestaurant.data.repository;

import android.content.Context;
import android.util.Log;
import com.example.sumayerestaurant.data.api.ApiService;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.LoginRequest;
import com.example.sumayerestaurant.data.model.LoginResponse;
import com.example.sumayerestaurant.util.Constants;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private Context context;
    private ApiService apiService;
    private TokenManager tokenManager;

    public AuthRepository(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getApiService(context);
        this.tokenManager = new TokenManager(context);
    }

    public void login(String username, String password, LoginCallback callback) {
        LoginRequest loginRequest = new LoginRequest(username, password);

        // ── DIAGNOSTIC: print the URL being called (never prints the password)
        Log.d(TAG, "LOGIN → POST " + Constants.BASE_URL + "api/auth/login"
                + " | username=" + username);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                // ── DIAGNOSTIC: always log HTTP status
                Log.d(TAG, "LOGIN onResponse | HTTP " + response.code()
                        + " | url=" + call.request().url());

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Save token and user  (token itself is NOT printed)
                    tokenManager.saveToken(loginResponse.getAccessToken());
                    tokenManager.saveUser(loginResponse.getUser());
                    tokenManager.setLoggedIn(true);

                    Log.d(TAG, "LOGIN SUCCESS | user=" + loginResponse.getUser().getUsername()
                            + " | roles=" + loginResponse.getUser().getRoles());
                    callback.onLoginSuccess(loginResponse);

                } else {
                    // ── DIAGNOSTIC: read the actual error body from the server
                    String errorBody = "(empty)";
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (IOException e) {
                            errorBody = "Could not read error body: " + e.getMessage();
                        }
                    }
                    Log.e(TAG, "LOGIN FAILED | HTTP " + response.code()
                            + " | errorBody=" + errorBody);

                    // Translate HTTP status into user-facing message
                    String userMessage;
                    switch (response.code()) {
                        case 401:
                            userMessage = Constants.ERROR_INVALID_CREDENTIALS;
                            break;
                        case 403:
                            userMessage = "Akaunti yako haijawashwa. Wasiliana na msimamizi.";
                            break;
                        case 500:
                        case 502:
                        case 503:
                            userMessage = Constants.ERROR_SERVER
                                    + " (HTTP " + response.code() + ")";
                            break;
                        default:
                            userMessage = Constants.ERROR_INVALID_CREDENTIALS
                                    + " (HTTP " + response.code() + ")";
                    }
                    callback.onLoginError(userMessage);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // ── DIAGNOSTIC: print the exact exception — this is what was hidden before
                Log.e(TAG, "LOGIN onFailure | exception=" + t.getClass().getName()
                        + " | message=" + t.getMessage(), t);

                // Determine the best user-facing error message
                String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";

                if (t instanceof java.net.ConnectException
                        || msg.contains("connect")
                        || msg.contains("refused")
                        || msg.contains("unable to resolve")) {
                    // Server is not running or wrong IP/port
                    Log.e(TAG, "LOGIN: Cannot reach server at " + Constants.BASE_URL
                            + " — is Spring Boot running?");
                    callback.onLoginError(
                            "Haiwezekani kufikia server (" + Constants.BASE_URL + "). "
                            + "Hakikisha Spring Boot inafanya kazi.");

                } else if (msg.contains("timeout") || msg.contains("timed out")) {
                    callback.onLoginError(Constants.ERROR_TIMEOUT);

                } else if (t instanceof java.io.IOException) {
                    Log.e(TAG, "LOGIN: IOException — " + t.getMessage());
                    callback.onLoginError(Constants.ERROR_NETWORK
                            + " (" + t.getMessage() + ")");

                } else if (msg.contains("json") || t instanceof com.google.gson.JsonSyntaxException) {
                    Log.e(TAG, "LOGIN: JSON parsing error — server response not valid JSON");
                    callback.onLoginError("Hitilafu ya JSON kutoka server. "
                            + "Angalia Logcat kwa maelezo zaidi.");

                } else {
                    callback.onLoginError(Constants.ERROR_SERVER
                            + " (" + t.getClass().getSimpleName() + ")");
                }
            }
        });
    }

    public void logout() {
        tokenManager.clearAll();
        RetrofitClient.reset();
    }

    public String getToken() {
        return tokenManager.getToken();
    }

    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    public interface LoginCallback {
        void onLoginSuccess(LoginResponse response);
        void onLoginError(String errorMessage);
    }
}
