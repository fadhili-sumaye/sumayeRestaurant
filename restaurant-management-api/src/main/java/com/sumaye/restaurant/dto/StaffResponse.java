package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Role;
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
public class StaffResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Long branchId;
    private String branchName;
    private Long restaurantId;
    private boolean active;
    private Set<String> roles;

    public static StaffResponse fromUser(User user) {
        return StaffResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .restaurantId(user.getRestaurant() != null ? user.getRestaurant().getId() : null)
                .active(user.isActive())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
