package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.Order;
import com.sumaye.restaurant.model.RestaurantTable;
import com.sumaye.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByClientRequestId(String clientRequestId);
    List<Order> findByBranchOrderByCreatedAtDesc(Branch branch);
    List<Order> findByWaiterOrderByCreatedAtDesc(User waiter);
    List<Order> findByTableAndStatusIn(RestaurantTable table, List<Order.OrderStatus> statuses);

    @Query("select o.status, count(o) from Order o where o.branch.id = :branchId and o.createdAt between :from and :to group by o.status")
    List<Object[]> countByStatusBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select o.createdAt from Order o where o.branch.id = :branchId and o.createdAt between :from and :to")
    List<LocalDateTime> findCreatedAtsBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select o.waiter.username, count(o), coalesce(sum(o.totalAmount),0) from Order o " +
            "where o.branch.id = :branchId and o.createdAt between :from and :to and o.status <> 'CANCELLED' and o.waiter is not null " +
            "group by o.waiter.username")
    List<Object[]> waiterStatsBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
