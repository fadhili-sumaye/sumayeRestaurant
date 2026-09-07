package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.KitchenOrder;
import com.sumaye.restaurant.model.KitchenOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitchenOrderItemRepository extends JpaRepository<KitchenOrderItem, Long> {
    List<KitchenOrderItem> findByKitchenOrder(KitchenOrder kitchenOrder);
}
