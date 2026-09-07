package com.sumaye.restaurant.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "daily_closings", uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "business_date"}))
@Data
public class DailyClosing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "branch_id", nullable = false) private Branch branch;
    @Column(name = "business_date", nullable = false) private LocalDate businessDate;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal totalSales = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal cashSales = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal mobileMoneySales = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal cardSales = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal bankSales = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal otherSales = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal discounts = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal expenses = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal expectedCash = BigDecimal.ZERO;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "closed_by_id", nullable = false) private User closedBy;
    @Column(nullable = false) private LocalDateTime closedAt = LocalDateTime.now();
    @Column(length = 500) private String notes;
}
