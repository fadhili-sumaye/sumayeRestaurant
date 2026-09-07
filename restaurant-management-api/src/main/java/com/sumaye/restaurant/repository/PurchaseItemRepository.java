package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Purchase;
import com.sumaye.restaurant.model.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    List<PurchaseItem> findByPurchase(Purchase purchase);
}
