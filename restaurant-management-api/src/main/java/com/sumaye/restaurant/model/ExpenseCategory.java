package com.sumaye.restaurant.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "expense_categories", uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "name"}))
@Data
public class ExpenseCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "branch_id", nullable = false) private Branch branch;
    @Column(nullable = false, length = 100) private String name;
    @Column(length = 300) private String description;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
