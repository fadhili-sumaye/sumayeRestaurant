package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Customer;
import com.sumaye.restaurant.model.Order;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CustomerProfileResponse {
    private Customer customer;
    private long visitCount;
    private BigDecimal totalSpent;
    private List<OrderResponse> orders;
}
