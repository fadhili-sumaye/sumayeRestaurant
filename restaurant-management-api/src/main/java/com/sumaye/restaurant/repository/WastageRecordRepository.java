package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.WastageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WastageRecordRepository extends JpaRepository<WastageRecord, Long> {
    List<WastageRecord> findByBranchOrderByCreatedAtDesc(Branch branch);
    List<WastageRecord> findByBranchIdOrderByCreatedAtDesc(Long branchId);

    @Query("select count(w), coalesce(sum(w.quantity), 0) from WastageRecord w " +
            "where w.branch.id = :branchId and w.createdAt between :from and :to")
    Object[] countAndQuantityBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
