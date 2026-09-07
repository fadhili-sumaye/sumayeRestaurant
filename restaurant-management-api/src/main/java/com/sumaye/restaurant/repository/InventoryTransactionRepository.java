package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.Ingredient;
import com.sumaye.restaurant.model.InventoryTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByBranchAndIngredientOrderByCreatedAtDesc(Branch branch, Ingredient ingredient);
    List<InventoryTransaction> findByBranchOrderByCreatedAtDesc(Branch branch, Pageable pageable);
    List<InventoryTransaction> findByBranchIdOrderByCreatedAtDesc(Long branchId);
    List<InventoryTransaction> findByBranchIdOrderByCreatedAtDesc(Long branchId, Pageable pageable);
    boolean existsByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
    List<InventoryTransaction> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}
