package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.TaxSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxSettingRepository extends JpaRepository<TaxSetting, Long> {
    List<TaxSetting> findByBranch(Branch branch);
    Optional<TaxSetting> findByBranchAndEnabledTrue(Branch branch);
}
