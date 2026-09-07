-- Restaurant Management System - MySQL Database Schema
-- Phase 6: Billing, Payments and Receipts

USE restaurant_management;

-- ============================================
-- 1. BILLS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS bills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bill_number VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL UNIQUE,
    branch_id BIGINT NOT NULL,
    table_id BIGINT,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_type ENUM('NONE', 'FIXED', 'PERCENTAGE') NOT NULL DEFAULT 'NONE',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_reason VARCHAR(255),
    discount_applied_by_id BIGINT,
    tax_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    amount_paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    balance_due DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_status ENUM('UNPAID', 'PARTIALLY_PAID', 'PAID', 'REFUNDED', 'CANCELLED') NOT NULL DEFAULT 'UNPAID',
    cashier_id BIGINT,
    bill_requested BOOLEAN NOT NULL DEFAULT FALSE,
    bill_requested_at TIMESTAMP NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_bill_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT fk_bill_table FOREIGN KEY (table_id) REFERENCES restaurant_tables(id) ON DELETE SET NULL,
    CONSTRAINT fk_bill_discount_user FOREIGN KEY (discount_applied_by_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_bill_cashier FOREIGN KEY (cashier_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_bill_number (bill_number),
    INDEX idx_bill_order (order_id),
    INDEX idx_bill_branch (branch_id),
    INDEX idx_bill_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. PAYMENTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    bill_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_method ENUM('CASH', 'MOBILE_MONEY', 'CARD', 'BANK', 'OTHER') NOT NULL DEFAULT 'CASH',
    provider ENUM('NONE', 'MPESA', 'AIRTEL_MONEY', 'MIXX', 'HALOPESA', 'VISA', 'MASTERCARD') NOT NULL DEFAULT 'NONE',
    transaction_reference VARCHAR(100),
    cash_received DECIMAL(10,2),
    change_given DECIMAL(10,2),
    status ENUM('SUCCESS', 'PENDING', 'FAILED', 'REVERSED') NOT NULL DEFAULT 'SUCCESS',
    cashier_id BIGINT NOT NULL,
    idempotency_key VARCHAR(100),
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_cashier FOREIGN KEY (cashier_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_payment_number (payment_number),
    INDEX idx_payment_bill (bill_id),
    INDEX idx_payment_order (order_id),
    INDEX idx_payment_idempotency (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. TAX_SETTINGS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS tax_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    branch_id BIGINT NOT NULL,
    tax_name VARCHAR(100) NOT NULL DEFAULT 'VAT',
    tax_rate DECIMAL(5,2) NOT NULL DEFAULT 18.00,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tax_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    INDEX idx_tax_branch (branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 4. AUDIT_LOGS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    performed_by_id BIGINT,
    branch_id BIGINT,
    details VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_performed_by FOREIGN KEY (performed_by_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_branch (branch_id),
    INDEX idx_audit_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
