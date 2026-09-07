package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.WastageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WastageRecordRepository extends JpaRepository<WastageRecord, Long> {
    List<WastageRecord> findByBranchOrderByCreatedAtDesc(Branch branch);
    List<WastageRecord> findByBranchIdOrderByCreatedAtDesc(Long branchId);
}
