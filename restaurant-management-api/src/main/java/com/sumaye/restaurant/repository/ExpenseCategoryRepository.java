package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.ExpenseCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    List<ExpenseCategory> findByBranchIdAndActiveTrueOrderByName(Long branchId);
    boolean existsByBranchIdAndNameIgnoreCase(Long branchId, String name);
}
