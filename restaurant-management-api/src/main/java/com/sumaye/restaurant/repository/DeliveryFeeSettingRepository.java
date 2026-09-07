package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.DeliveryFeeSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryFeeSettingRepository extends JpaRepository<DeliveryFeeSetting, Long> {
    Optional<DeliveryFeeSetting> findByBranchId(Long branchId);
}
