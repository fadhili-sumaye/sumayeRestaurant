package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private Long branchId;
        private Long restaurantId;
        private boolean active;
        private Set<String> roles;

        public static UserInfo fromUser(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                    .restaurantId(user.getRestaurant() != null ? user.getRestaurant().getId() : null)
                    .active(user.isActive())
                    .roles(user.getRoles().stream()
                            .map(r -> r.getName())
                            .collect(Collectors.toSet()))
                    .build();
        }
    }
}
