package com.sumaye.restaurant.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kitchen_orders")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class KitchenOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kitchen_user_id")
    private User kitchenUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KitchenOrderStatus status = KitchenOrderStatus.NEW;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "kitchenOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KitchenOrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime acceptedAt;

    @Column
    private LocalDateTime preparingAt;

    @Column
    private LocalDateTime readyAt;

    @Column
    private LocalDateTime cancelledAt;

    @Column
    private LocalDateTime updatedAt;

    public enum KitchenOrderStatus {
        NEW,
        ACCEPTED,
        PREPARING,
        READY,
        CANCELLED
    }
}
