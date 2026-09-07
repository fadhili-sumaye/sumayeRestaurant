package com.example.sumayerestaurant.data.websocket;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.util.Constants;

public class WebSocketManager {
    private static WebSocketManager instance;
    private final Context context;
    private final TokenManager tokenManager;
    private StompClient stompClient;
    private final MutableLiveData<StompClient.ConnectionState> connectionStateLiveData = new MutableLiveData<>(StompClient.ConnectionState.DISCONNECTED);

    private WebSocketManager(Context context) {
        this.context = context.getApplicationContext();
        this.tokenManager = new TokenManager(this.context);
    }

    public static synchronized WebSocketManager getInstance(Context context) {
        if (instance == null) {
            instance = new WebSocketManager(context);
        }
        return instance;
    }

    public synchronized void connect() {
        String token = tokenManager.getToken();
        String wsUrl = Constants.WEBSOCKET_URL;

        if (stompClient == null) {
            stompClient = new StompClient(wsUrl, token);
            stompClient.addConnectionListener(new StompClient.ConnectionListener() {
                @Override
                public void onStateChanged(StompClient.ConnectionState state) {
                    connectionStateLiveData.postValue(state);
                }

                @Override
                public void onError(String message) {
                    // Handled internally
                }
            });
        }

        stompClient.connect();
    }

    public synchronized void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }
        connectionStateLiveData.postValue(StompClient.ConnectionState.DISCONNECTED);
    }

    public synchronized void subscribe(String destination, StompClient.MessageListener listener) {
        if (stompClient == null) {
            connect();
        }
        if (stompClient != null) {
            stompClient.subscribe(destination, listener);
        }
    }

    public synchronized void unsubscribe(String destination) {
        if (stompClient != null) {
            stompClient.unsubscribe(destination);
        }
    }

    public LiveData<StompClient.ConnectionState> getConnectionStateLiveData() {
        return connectionStateLiveData;
    }
}
