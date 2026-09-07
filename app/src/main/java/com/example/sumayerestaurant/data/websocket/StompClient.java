package com.example.sumayerestaurant.data.websocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import okhttp3.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class StompClient {
    private static final String TAG = "StompClient";
    private static final String NULL_CHAR = "\u0000";

    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    public interface ConnectionListener {
        void onStateChanged(ConnectionState state);
        void onError(String message);
    }

    public interface MessageListener {
        void onMessage(String destination, String payload);
    }

    private final String url;
    private final String authToken;
    private final OkHttpClient okHttpClient;
    private WebSocket webSocket;
    private ConnectionState currentState = ConnectionState.DISCONNECTED;
    private final List<ConnectionListener> connectionListeners = new ArrayList<>();
    private final Map<String, String> activeSubscriptions = new ConcurrentHashMap<>(); // destination -> subId
    private final Map<String, MessageListener> topicListeners = new ConcurrentHashMap<>(); // destination -> listener
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isManuallyClosed = false;
    private int reconnectAttempts = 0;
    private int subCounter = 0;

    public StompClient(String url, String authToken) {
        this.url = url;
        this.authToken = authToken;
        this.okHttpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // No timeout for WebSockets
                .pingInterval(10, TimeUnit.SECONDS)
                .build();
    }

    public synchronized void connect() {
        if (currentState == ConnectionState.CONNECTED || currentState == ConnectionState.CONNECTING) {
            return;
        }

        isManuallyClosed = false;
        updateState(ConnectionState.CONNECTING);

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d(TAG, "Underlying WebSocket opened. Sending STOMP CONNECT frame...");
                sendConnectFrame();
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                handleStompFrame(text);
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                Log.d(TAG, "WebSocket closing: " + reason);
                ws.close(1000, null);
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.d(TAG, "WebSocket closed: " + reason);
                updateState(ConnectionState.DISCONNECTED);
                scheduleReconnect();
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WebSocket failure: " + t.getMessage());
                updateState(ConnectionState.DISCONNECTED);
                notifyError(t.getMessage());
                scheduleReconnect();
            }
        });
    }

    public synchronized void disconnect() {
        isManuallyClosed = true;
        reconnectAttempts = 0;
        if (webSocket != null) {
            try {
                sendFrame("DISCONNECT", null, null);
                webSocket.close(1000, "Client disconnect");
            } catch (Exception ignored) {}
            webSocket = null;
        }
        updateState(ConnectionState.DISCONNECTED);
    }

    public synchronized void subscribe(String destination, MessageListener listener) {
        topicListeners.put(destination, listener);

        if (currentState == ConnectionState.CONNECTED) {
            sendSubscription(destination);
        }
    }

    public synchronized void unsubscribe(String destination) {
        String subId = activeSubscriptions.remove(destination);
        topicListeners.remove(destination);

        if (subId != null && currentState == ConnectionState.CONNECTED) {
            Map<String, String> headers = new HashMap<>();
            headers.put("id", subId);
            sendFrame("UNSUBSCRIBE", headers, null);
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        if (!connectionListeners.contains(listener)) {
            connectionListeners.add(listener);
            listener.onStateChanged(currentState);
        }
    }

    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    private void sendConnectFrame() {
        StringBuilder frame = new StringBuilder();
        frame.append("CONNECT\n");
        frame.append("accept-version:1.1,1.2\n");
        frame.append("heart-beat:10000,10000\n");
        if (authToken != null && !authToken.isBlank()) {
            frame.append("Authorization:Bearer ").append(authToken).append("\n");
            frame.append("passcode:").append(authToken).append("\n");
        }
        frame.append("\n");
        frame.append(NULL_CHAR);

        if (webSocket != null) {
            webSocket.send(frame.toString());
        }
    }

    private void sendSubscription(String destination) {
        String subId = "sub-" + (++subCounter);
        activeSubscriptions.put(destination, subId);

        Map<String, String> headers = new HashMap<>();
        headers.put("id", subId);
        headers.put("destination", destination);
        headers.put("ack", "auto");

        sendFrame("SUBSCRIBE", headers, null);
        Log.d(TAG, "Subscribed to " + destination + " with ID " + subId);
    }

    private void sendFrame(String command, Map<String, String> headers, String body) {
        if (webSocket == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append(command).append("\n");
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
            }
        }
        sb.append("\n");
        if (body != null) {
            sb.append(body);
        }
        sb.append(NULL_CHAR);

        webSocket.send(sb.toString());
    }

    private void handleStompFrame(String rawText) {
        if (rawText == null || rawText.isEmpty()) return;

        // Strip null character
        String text = rawText.replace(NULL_CHAR, "").trim();
        if (text.isEmpty()) return; // heartbeat

        String[] lines = text.split("\n");
        if (lines.length == 0) return;

        String command = lines[0].trim();
        Map<String, String> headers = new HashMap<>();
        int i = 1;
        while (i < lines.length && !lines[i].trim().isEmpty()) {
            String line = lines[i];
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String key = line.substring(0, colonIndex).trim();
                String val = line.substring(colonIndex + 1).trim();
                headers.put(key, val);
            }
            i++;
        }

        StringBuilder bodyBuilder = new StringBuilder();
        for (int j = i + 1; j < lines.length; j++) {
            bodyBuilder.append(lines[j]);
            if (j < lines.length - 1) bodyBuilder.append("\n");
        }
        String body = bodyBuilder.toString();

        if ("CONNECTED".equalsIgnoreCase(command)) {
            Log.d(TAG, "STOMP session connected!");
            reconnectAttempts = 0;
            updateState(ConnectionState.CONNECTED);

            // Re-subscribe all pending topics
            for (String destination : topicListeners.keySet()) {
                sendSubscription(destination);
            }
        } else if ("MESSAGE".equalsIgnoreCase(command)) {
            String destination = headers.get("destination");
            if (destination != null) {
                MessageListener listener = topicListeners.get(destination);
                if (listener != null) {
                    mainHandler.post(() -> listener.onMessage(destination, body));
                }
            }
        } else if ("ERROR".equalsIgnoreCase(command)) {
            Log.e(TAG, "STOMP ERROR received: " + headers.get("message") + " -> " + body);
            notifyError(headers.getOrDefault("message", "Hitilafu ya WebSocket"));
        }
    }

    private void scheduleReconnect() {
        if (isManuallyClosed) return;

        reconnectAttempts++;
        long delayMs = Math.min(30000, (long) Math.pow(2, Math.min(reconnectAttempts, 5)) * 1000);
        Log.d(TAG, "Scheduling reconnect in " + delayMs + " ms (Attempt " + reconnectAttempts + ")");

        mainHandler.postDelayed(() -> {
            if (!isManuallyClosed && currentState == ConnectionState.DISCONNECTED) {
                connect();
            }
        }, delayMs);
    }

    private void updateState(ConnectionState state) {
        this.currentState = state;
        mainHandler.post(() -> {
            for (ConnectionListener l : new ArrayList<>(connectionListeners)) {
                l.onStateChanged(state);
            }
        });
    }

    private void notifyError(String message) {
        mainHandler.post(() -> {
            for (ConnectionListener l : new ArrayList<>(connectionListeners)) {
                l.onError(message);
            }
        });
    }

    public ConnectionState getCurrentState() {
        return currentState;
    }
}
