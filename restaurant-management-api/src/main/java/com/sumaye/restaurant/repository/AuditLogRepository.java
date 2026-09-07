package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.AuditLog;
import com.sumaye.restaurant.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByBranchOrderByCreatedAtDesc(Branch branch);
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
}
