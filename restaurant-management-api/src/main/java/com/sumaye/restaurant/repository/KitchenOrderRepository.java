package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.KitchenOrder;
import com.sumaye.restaurant.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KitchenOrderRepository extends JpaRepository<KitchenOrder, Long> {
    Optional<KitchenOrder> findByOrder(Order order);
    List<KitchenOrder> findByBranchOrderByCreatedAtDesc(Branch branch);
    List<KitchenOrder> findByBranchAndStatusInOrderByCreatedAtAsc(Branch branch, List<KitchenOrder.KitchenOrderStatus> statuses);
    List<KitchenOrder> findByBranchAndStatusInOrderByCreatedAtDesc(Branch branch, List<KitchenOrder.KitchenOrderStatus> statuses);
}
