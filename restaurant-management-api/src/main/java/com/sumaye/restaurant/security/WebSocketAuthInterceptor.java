package com.sumaye.restaurant.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            String token = null;

            if (authHeaders != null && !authHeaders.isEmpty()) {
                String bearerToken = authHeaders.get(0);
                if (bearerToken.startsWith("Bearer ")) {
                    token = bearerToken.substring(7);
                } else {
                    token = bearerToken;
                }
            } else {
                // Check passcode header fallback
                String passcode = accessor.getPasscode();
                if (passcode != null && !passcode.isBlank()) {
                    token = passcode;
                }
            }

            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                accessor.setUser(auth);
                log.info("WebSocket connection authenticated successfully for user: {}", auth.getName());
            } else {
                log.warn("Unauthorized WebSocket connection attempt: missing or invalid JWT");
                throw new IllegalArgumentException("Unauthorized WebSocket connection");
            }
        }

        return message;
    }
}
