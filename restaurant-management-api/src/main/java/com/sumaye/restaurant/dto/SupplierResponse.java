package com.sumaye.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private Long id;
    private Long branchId;
    private String branchName;
    private String name;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private boolean active;
    private LocalDateTime createdAt;
}
