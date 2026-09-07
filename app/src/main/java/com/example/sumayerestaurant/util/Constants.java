package com.example.sumayerestaurant.util;

/**
 * Application-wide constants.
 *
 * =========================================================
 *  HOW TO SWITCH ENVIRONMENTS
 * =========================================================
 *  Change the ENV field to one of:
 *
 *    Env.DEV_EMULATOR  → Android Emulator on local machine (10.0.2.2:8080)
 *    Env.DEV_LAN       → Physical device on the same WiFi network
 *                         → Edit LAN_IP below with your PC's local IP (e.g. 192.168.1.25)
 *    Env.PRODUCTION    → Cloud server (edit PRODUCTION_API_URL below with your domain)
 *
 *  DO NOT scatter URLs throughout activities or repositories.
 *  ALL networking goes through RetrofitClient which reads Constants.BASE_URL.
 * =========================================================
 */
public class Constants {

    // =========================================================
    //  ENVIRONMENT SELECTOR  ← CHANGE THIS ONE LINE ONLY
    // =========================================================
    public static final Env ENV = Env.DEV_LAN;
 
    // =========================================================
    //  URLs — Only edit these, not other files
    // =========================================================
    private static final String EMULATOR_URL    = "http://10.0.2.2:8080/";
    private static final String LAN_IP          = "192.168.118.71";         // ← Your PC's local Wi-Fi IP
    private static final String ACTIVE_LAN_IP   = "172.20.10.2";
    private static final String LAN_URL         = "http://" + ACTIVE_LAN_IP + ":8080/";
    private static final String PRODUCTION_URL  = "https://api.yourdomain.com/"; // ← Edit: your cloud domain

    // Derived BASE_URL (used by RetrofitClient and WebSocketManager)
    public static final String BASE_URL;
    public static final String WEBSOCKET_URL;

    static {
        switch (ENV) {
            case PRODUCTION:
                String prodUrl = PRODUCTION_URL.endsWith("/") ? PRODUCTION_URL : PRODUCTION_URL + "/";
                BASE_URL     = prodUrl;
                WEBSOCKET_URL = prodUrl.replace("https://", "wss://")
                                       .replace("http://", "ws://") + "ws";
                break;
            case DEV_LAN:
                BASE_URL     = LAN_URL;
                WEBSOCKET_URL = "ws://" + ACTIVE_LAN_IP + ":8080/ws";
                break;
            case DEV_EMULATOR:
            default:
                BASE_URL     = EMULATOR_URL;
                WEBSOCKET_URL = "ws://10.0.2.2:8080/ws";
                break;
        }
    }

    // =========================================================
    //  Timeout settings
    // =========================================================
    public static final int CONNECT_TIMEOUT = 15; // seconds
    public static final int READ_TIMEOUT    = 30;
    public static final int WRITE_TIMEOUT   = 30;

    // =========================================================
    //  SharedPreferences Keys
    // =========================================================
    public static final String PREF_NAME   = "sumaye_restaurant_prefs";
    public static final String TOKEN_KEY   = "auth_token";
    public static final String USER_KEY    = "user_data";
    public static final String IS_LOGGED_IN = "is_logged_in";

    // =========================================================
    //  API Endpoints
    // =========================================================
    public static final String LOGIN_ENDPOINT      = "/api/auth/login";
    public static final String LOGOUT_ENDPOINT     = "/api/auth/logout";
    public static final String GET_PROFILE_ENDPOINT = "/api/users/me";

    // =========================================================
    //  User Roles
    // =========================================================
    public static final String ROLE_OWNER        = "ROLE_OWNER";
    public static final String ROLE_ADMIN        = "ROLE_ADMIN";
    public static final String ROLE_MANAGER      = "ROLE_MANAGER";
    public static final String ROLE_WAITER       = "ROLE_WAITER";
    public static final String ROLE_CASHIER      = "ROLE_CASHIER";
    public static final String ROLE_KITCHEN      = "ROLE_KITCHEN";
    public static final String ROLE_STOREKEEPER  = "ROLE_STOREKEEPER";
    public static final String ROLE_DELIVERY     = "ROLE_DELIVERY";

    // =========================================================
    //  Error Messages (Kiswahili)
    // =========================================================
    public static final String ERROR_NETWORK             = "Hakuna muunganisho wa mtandao. Tafadhali angalia muunganisho wako.";
    public static final String ERROR_INVALID_CREDENTIALS = "Jina la mtumiaji au nenosiri si sahihi.";
    public static final String ERROR_SERVER              = "Hitilafu ya server. Tafadhali jaribu baadaye.";
    public static final String ERROR_TIMEOUT             = "Muda umekwisha katika kuomba. Tafadhali jaribu tena.";
    public static final String ERROR_UNAUTHORIZED        = "Umesalia muda wako. Tafadhali ingia tena.";

    // =========================================================
    //  Environment Enum
    // =========================================================
    public enum Env {
        DEV_EMULATOR,  // Android Emulator on local development machine
        DEV_LAN,       // Physical phone on the same WiFi as development PC
        PRODUCTION      // Cloud server deployment
    }

    private Constants() { /* Prevent instantiation */ }
}
