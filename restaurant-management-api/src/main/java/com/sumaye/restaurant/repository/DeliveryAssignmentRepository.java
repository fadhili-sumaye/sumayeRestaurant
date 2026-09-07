package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {
    List<DeliveryAssignment> findByDeliveryOrderIdOrderByAssignedAtDesc(Long deliveryOrderId);
}
