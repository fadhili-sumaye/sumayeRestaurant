package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.MenuCategory;
import com.sumaye.restaurant.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByBranchOrderByNameAsc(Branch branch);
    List<MenuItem> findByCategory(MenuCategory category);
    List<MenuItem> findByBranchAndAvailable(Branch branch, boolean available);
    List<MenuItem> findByAvailableOrderByNameAsc(boolean available);
}
