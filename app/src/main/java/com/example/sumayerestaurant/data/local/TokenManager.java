package com.example.sumayerestaurant.data.local;

import android.content.Context;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.example.sumayerestaurant.util.Constants;
import com.google.gson.Gson;
import com.example.sumayerestaurant.data.model.User;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {
    private Context context;
    private EncryptedSharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        this.context = context;
        initializeEncryptedPreferences();
    }

    private void initializeEncryptedPreferences() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    context,
                    Constants.PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToken(String token) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(Constants.TOKEN_KEY, token).apply();
        }
    }

    public String getToken() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(Constants.TOKEN_KEY, null);
        }
        return null;
    }

    public void saveUser(User user) {
        if (sharedPreferences != null) {
            String userJson = new Gson().toJson(user);
            sharedPreferences.edit().putString(Constants.USER_KEY, userJson).apply();
        }
    }

    public User getUser() {
        if (sharedPreferences != null) {
            String userJson = sharedPreferences.getString(Constants.USER_KEY, null);
            if (userJson != null) {
                return new Gson().fromJson(userJson, User.class);
            }
        }
        return null;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(Constants.IS_LOGGED_IN, isLoggedIn).apply();
        }
    }

    public boolean isLoggedIn() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(Constants.IS_LOGGED_IN, false);
        }
        return false;
    }

    public void clearAll() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().apply();
        }
    }
}
