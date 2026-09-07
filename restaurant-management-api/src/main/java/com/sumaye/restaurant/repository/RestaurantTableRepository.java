package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    Optional<RestaurantTable> findByQrToken(String qrToken);

    /** Serializes public orders for a table, preventing two scans from opening it at once. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RestaurantTable t where t.qrToken = :qrToken")
    Optional<RestaurantTable> findByQrTokenForUpdate(@Param("qrToken") String qrToken);

    List<RestaurantTable> findByBranchOrderByTableNumberAsc(Branch branch);
    List<RestaurantTable> findByBranchAndStatus(Branch branch, RestaurantTable.TableStatus status);
    List<RestaurantTable> findByBranch(Branch branch);
    Optional<RestaurantTable> findByBranchAndTableNumber(Branch branch, Integer tableNumber);
    boolean existsByBranchAndTableNumber(Branch branch, Integer tableNumber);
}
