package com.sumaye.restaurant.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "cash_reconciliations")
@Data
public class CashReconciliation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "daily_closing_id", nullable = false, unique = true) private DailyClosing dailyClosing;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal expectedCash;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal actualCash;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal difference;
    @Column(length = 500) private String differenceReason;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recorded_by_id", nullable = false) private User recordedBy;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
}
