package com.sumaye.restaurant.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_fee_settings")
@Data
public class DeliveryFeeSetting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false, unique = true)
    private Branch branch;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fixedFee = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean enabled = true;

    private LocalDateTime updatedAt;
}
