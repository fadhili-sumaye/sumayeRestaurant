package com.sumaye.restaurant.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrTableMenuResponse {
    private String restaurantName;
    private String branchName;
    private Integer tableNumber;
    private List<QrMenuItemResponse> menuItems;
}
