package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {
    List<DeliveryOrder> findByBranchIdAndStatus(Long branchId, DeliveryOrder.Status status);
    List<DeliveryOrder> findByBranchIdOrderByCreatedAtDesc(Long branchId);
    List<DeliveryOrder> findByBranchIdAndRiderIdOrderByCreatedAtDesc(Long branchId, Long riderId);
    List<DeliveryOrder> findByRiderIdOrderByCreatedAtDesc(Long riderId);
    Optional<DeliveryOrder> findByOrderId(Long orderId);
}
