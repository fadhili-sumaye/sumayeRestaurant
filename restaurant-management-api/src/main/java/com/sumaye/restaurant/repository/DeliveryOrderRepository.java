package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {
    List<DeliveryOrder> findByBranchIdAndStatus(Long branchId, DeliveryOrder.Status status);
    List<DeliveryOrder> findByBranchIdOrderByCreatedAtDesc(Long branchId);
    List<DeliveryOrder> findByBranchIdAndRiderIdOrderByCreatedAtDesc(Long branchId, Long riderId);
    List<DeliveryOrder> findByRiderIdOrderByCreatedAtDesc(Long riderId);
    Optional<DeliveryOrder> findByOrderId(Long orderId);

    @Query("select count(d) from DeliveryOrder d where d.branch.id = :branchId and d.createdAt between :from and :to and d.status in ('DELIVERED','COMPLETED')")
    long completedCountBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select count(d) from DeliveryOrder d where d.branch.id = :branchId and d.createdAt between :from and :to and d.status = 'CANCELLED'")
    long cancelledCountBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select d.rider.username, count(d), coalesce(sum(d.deliveryFee), 0) from DeliveryOrder d " +
            "where d.branch.id = :branchId and d.createdAt between :from and :to and d.rider is not null and d.status in ('DELIVERED','COMPLETED') " +
            "group by d.rider.username")
    List<Object[]> riderStatsBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
