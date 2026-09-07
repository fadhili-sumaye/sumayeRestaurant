package com.sumaye.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Jina la mtumiaji linahitajika")
    private String username;

    @NotBlank(message = "Nenosiri linahitajika")
    private String password;
}
