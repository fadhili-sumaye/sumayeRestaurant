package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.KitchenOrder;
import com.sumaye.restaurant.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KitchenOrderRepository extends JpaRepository<KitchenOrder, Long> {
    Optional<KitchenOrder> findByOrder(Order order);
    List<KitchenOrder> findByBranchOrderByCreatedAtDesc(Branch branch);
    List<KitchenOrder> findByBranchAndStatusInOrderByCreatedAtAsc(Branch branch, List<KitchenOrder.KitchenOrderStatus> statuses);
    List<KitchenOrder> findByBranchAndStatusInOrderByCreatedAtDesc(Branch branch, List<KitchenOrder.KitchenOrderStatus> statuses);

    @Query("select count(k) from KitchenOrder k where k.branch.id = :branchId and k.createdAt between :from and :to")
    long countBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select k.kitchenUser.username, count(k) from KitchenOrder k " +
            "where k.branch.id = :branchId and k.createdAt between :from and :to and k.kitchenUser is not null " +
            "group by k.kitchenUser.username")
    List<Object[]> preparedByUserBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
