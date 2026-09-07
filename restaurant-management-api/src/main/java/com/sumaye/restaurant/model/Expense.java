package com.sumaye.restaurant.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "expenses", indexes = {@Index(name = "idx_expense_branch_date", columnList = "branch_id,expense_date"), @Index(name = "idx_expense_category", columnList = "expense_category_id")})
@Data
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "branch_id", nullable = false) private Branch branch;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "expense_category_id", nullable = false) private ExpenseCategory category;
    @Column(nullable = false, length = 300) private String description;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Payment.PaymentMethod paymentMethod;
    @Column(length = 100) private String referenceNumber;
    @Column(name = "expense_date", nullable = false) private LocalDate expenseDate;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recorded_by_id", nullable = false) private User recordedBy;
    @Column(length = 500) private String notes;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status = Status.POSTED;
    @Column(length = 500) private String voidReason;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "voided_by_id") private User voidedBy;
    private LocalDateTime voidedAt;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    public enum Status { POSTED, VOIDED }
}
