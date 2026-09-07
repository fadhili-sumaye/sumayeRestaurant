package com.sumaye.restaurant.dto; import jakarta.validation.constraints.*; import lombok.Data;
@Data public class CustomerRequest { @NotBlank private String fullName; @NotBlank @Pattern(regexp="^(\\+255|0)[0-9]{9}$",message="Weka namba sahihi ya Tanzania") private String phoneNumber; private String email; private String address; private String notes; }
