package com.sumaye.restaurant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class CreateSupplierRequest {

    @NotBlank(message = "Jina la msambazaji linahitajika")
    private String name;

    private String contactPhone;
    private String contactEmail;
    private String address;
}
