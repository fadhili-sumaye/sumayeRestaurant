package com.example.sumayerestaurant.util;

import com.example.sumayerestaurant.BuildConfig;

/** Application-wide configuration and shared constants. */
public final class Constants {
    /** Selected by Gradle: debug targets the emulator and release targets HTTPS production. */
    public static final String BASE_URL = BuildConfig.API_BASE_URL;
    public static final String WEBSOCKET_URL = BASE_URL
            .replace("https://", "wss://")
            .replace("http://", "ws://") + "ws";

    public static final int CONNECT_TIMEOUT = 45;
    public static final int READ_TIMEOUT = 60;
    public static final int WRITE_TIMEOUT = 60;

    public static final String PREF_NAME = "sumaye_restaurant_prefs";
    public static final String TOKEN_KEY = "auth_token";
    public static final String USER_KEY = "user_data";
    public static final String IS_LOGGED_IN = "is_logged_in";

    public static final String LOGIN_ENDPOINT = "/api/auth/login";
    public static final String LOGOUT_ENDPOINT = "/api/auth/logout";
    public static final String GET_PROFILE_ENDPOINT = "/api/users/me";

    public static final String ROLE_OWNER = "ROLE_OWNER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_WAITER = "ROLE_WAITER";
    public static final String ROLE_CASHIER = "ROLE_CASHIER";
    public static final String ROLE_KITCHEN = "ROLE_KITCHEN";
    public static final String ROLE_STOREKEEPER = "ROLE_STOREKEEPER";
    public static final String ROLE_DELIVERY = "ROLE_DELIVERY";

    public static final String ERROR_NETWORK = "Hakuna muunganisho wa mtandao. Tafadhali angalia muunganisho wako.";
    public static final String ERROR_INVALID_CREDENTIALS = "Jina la mtumiaji au nenosiri si sahihi.";
    public static final String ERROR_SERVER = "Hitilafu ya server. Tafadhali jaribu baadaye.";
    public static final String ERROR_TIMEOUT = "Muda umekwisha katika kuomba. Tafadhali jaribu tena.";
    public static final String ERROR_UNAUTHORIZED = "Umesalia muda wako. Tafadhali ingia tena.";

    private Constants() { }
}
