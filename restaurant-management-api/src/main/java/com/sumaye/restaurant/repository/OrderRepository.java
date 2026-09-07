package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.Order;
import com.sumaye.restaurant.model.RestaurantTable;
import com.sumaye.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByClientRequestId(String clientRequestId);
    List<Order> findByBranchOrderByCreatedAtDesc(Branch branch);
    List<Order> findByWaiterOrderByCreatedAtDesc(User waiter);
    List<Order> findByTableAndStatusIn(RestaurantTable table, List<Order.OrderStatus> statuses);
}
