package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Order;
import com.sumaye.restaurant.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);

    @Query("select oi.menuItem.category.name, coalesce(sum(oi.subtotal),0) from OrderItem oi " +
            "where oi.order.branch.id = :branchId and oi.order.createdAt between :from and :to and oi.order.status <> 'CANCELLED' " +
            "group by oi.menuItem.category.name")
    List<Object[]> revenueByCategory(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select oi.menuItem.id, oi.menuItem.name, oi.menuItem.category.name, sum(oi.quantity), coalesce(sum(oi.subtotal),0) " +
            "from OrderItem oi " +
            "where oi.order.branch.id = :branchId and oi.order.createdAt between :from and :to and oi.order.status <> 'CANCELLED' " +
            "group by oi.menuItem.id, oi.menuItem.name, oi.menuItem.category.name order by sum(oi.quantity) desc")
    List<Object[]> bestSellers(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
