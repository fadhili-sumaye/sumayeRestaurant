-- Restaurant Management System - MySQL Database Schema
-- Phase 5: Kitchen Management and Real-Time KOT System

USE restaurant_management;

-- ============================================
-- 1. UPDATE KITCHEN_ORDERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS kitchen_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL UNIQUE,
    branch_id BIGINT NOT NULL,
    kitchen_user_id BIGINT,
    status ENUM('NEW', 'ACCEPTED', 'PREPARING', 'READY', 'CANCELLED') DEFAULT 'NEW',
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP NULL,
    preparing_at TIMESTAMP NULL,
    ready_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kot_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_kot_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT fk_kot_kitchen_user FOREIGN KEY (kitchen_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_kot_order (order_id),
    INDEX idx_kot_branch (branch_id),
    INDEX idx_kot_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. KITCHEN_ORDER_ITEMS TABLE (PARTIAL ITEM STATUS)
-- ============================================
CREATE TABLE IF NOT EXISTS kitchen_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    kitchen_order_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    special_instructions VARCHAR(255),
    status ENUM('NEW', 'PREPARING', 'READY', 'CANCELLED') DEFAULT 'NEW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_koi_kitchen_order FOREIGN KEY (kitchen_order_id) REFERENCES kitchen_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_koi_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    INDEX idx_koi_kitchen_order (kitchen_order_id),
    INDEX idx_koi_order_item (order_item_id),
    INDEX idx_koi_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
