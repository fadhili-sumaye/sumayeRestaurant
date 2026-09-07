package com.sumaye.restaurant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStaffRequest {
    @NotBlank(message = "Jina la mtumiaji linahitajika")
    @Size(min = 3, max = 50, message = "Jina la mtumiaji liwe kati ya herufi 3 na 50")
    private String username;

    @NotBlank(message = "Nenosiri linahitajika")
    @Size(min = 6, message = "Nenosiri liwe na angalau herufi 6")
    private String password;

    @NotBlank(message = "Jina la kwanza linahitajika")
    private String firstName;

    @NotBlank(message = "Jina la ukoo linahitajika")
    private String lastName;

    @Email(message = "Barua pepe si sahihi")
    private String email;

    private Long branchId;

    @NotBlank(message = "Wajibu (role) unahitajika")
    private String role;
}
