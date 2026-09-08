package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    List<MenuCategory> findByBranchOrderByNameAsc(Branch branch);
    List<MenuCategory> findByBranchAndActive(Branch branch, boolean active);
    List<MenuCategory> findByActiveOrderByNameAsc(boolean active);
}
