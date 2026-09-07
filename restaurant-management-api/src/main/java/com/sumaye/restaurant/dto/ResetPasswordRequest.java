package com.sumaye.restaurant.dto;

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
public class ResetPasswordRequest {
    @NotBlank(message = "Jina la mtumiaji linahitajika")
    private String username;

    @NotBlank(message = "Nenosiri jipya linahitajika")
    @Size(min = 6, message = "Nenosiri jipya lazima liwe na angalau herufi 6")
    private String newPassword;
}
