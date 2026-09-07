package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.DailyClosing;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyClosingRepository extends JpaRepository<DailyClosing, Long> {
    Optional<DailyClosing> findByBranchIdAndBusinessDate(Long branchId, LocalDate date);
}
