package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.Ingredient;
import com.sumaye.restaurant.model.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {
    List<InventoryStock> findByBranch(Branch branch);
    Optional<InventoryStock> findByBranchAndIngredient(Branch branch, Ingredient ingredient);
    Optional<InventoryStock> findByBranchIdAndIngredientId(Long branchId, Long ingredientId);

    @Query("SELECT s FROM InventoryStock s WHERE s.branch.id = :branchId AND s.quantityOnHand <= s.minimumStockLevel")
    List<InventoryStock> findLowStockByBranchId(@Param("branchId") Long branchId);

    @Query("select count(s) from InventoryStock s where s.branch.id = :branchId")
    long countByBranchId(@Param("branchId") Long branchId);

    @Query("select count(s) from InventoryStock s where s.branch.id = :branchId and s.quantityOnHand <= s.minimumStockLevel")
    long countLowStockByBranchId(@Param("branchId") Long branchId);

    @Query("select count(s) from InventoryStock s where s.branch.id = :branchId and s.quantityOnHand <= 0")
    long countOutOfStockByBranchId(@Param("branchId") Long branchId);

    @Query("select coalesce(sum(s.quantityOnHand * s.costPerUnit), 0) from InventoryStock s where s.branch.id = :branchId")
    BigDecimal stockValueByBranchId(@Param("branchId") Long branchId);
}
