-- Restaurant Management System - MySQL Database Schema
-- Phase 1: Initial Schema with Authentication & Roles

-- Drop existing database if needed (for fresh start)
-- DROP DATABASE IF EXISTS restaurant_management;
-- CREATE DATABASE restaurant_management;
-- USE restaurant_management;

USE restaurant_management;

-- ============================================
-- 1. RESTAURANTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS restaurants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    logo_url VARCHAR(500),
    tin_number VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(500),
    city VARCHAR(100),
    country VARCHAR(100),
    currency_code VARCHAR(3) DEFAULT 'TZS',
    website VARCHAR(255),
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. BRANCHES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS branches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(500),
    city VARCHAR(100),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    opening_time TIME,
    closing_time TIME,
    status ENUM('ACTIVE', 'INACTIVE', 'CLOSED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_branch_restaurant FOREIGN KEY (restaurant_id) 
        REFERENCES restaurants(id) ON DELETE CASCADE,
    INDEX idx_restaurant (restaurant_id),
    INDEX idx_status (status),
    UNIQUE KEY unique_branch_name (restaurant_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. ROLES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'System administrator - full access'),
('ROLE_OWNER', 'Restaurant owner - business level access'),
('ROLE_MANAGER', 'Branch manager - operational access'),
('ROLE_WAITER', 'Waiter - order management access'),
('ROLE_CASHIER', 'Cashier - payment processing access'),
('ROLE_KITCHEN', 'Kitchen staff - order preparation access'),
('ROLE_CUSTOMER', 'Customer - limited access for ordering');

-- ============================================
-- 4. PERMISSIONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    resource VARCHAR(100),
    action VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default permissions
INSERT INTO permissions (name, description, resource, action) VALUES
-- User Management
('USER_CREATE', 'Create new users', 'USER', 'CREATE'),
('USER_READ', 'View user details', 'USER', 'READ'),
('USER_UPDATE', 'Update user information', 'USER', 'UPDATE'),
('USER_DELETE', 'Delete users', 'USER', 'DELETE'),

-- Branch Management
('BRANCH_CREATE', 'Create new branches', 'BRANCH', 'CREATE'),
('BRANCH_READ', 'View branch details', 'BRANCH', 'READ'),
('BRANCH_UPDATE', 'Update branch information', 'BRANCH', 'UPDATE'),
('BRANCH_DELETE', 'Delete branches', 'BRANCH', 'DELETE'),

-- Table Management
('TABLE_CREATE', 'Create tables', 'TABLE', 'CREATE'),
('TABLE_READ', 'View tables', 'TABLE', 'READ'),
('TABLE_UPDATE', 'Update table status', 'TABLE', 'UPDATE'),
('TABLE_DELETE', 'Delete tables', 'TABLE', 'DELETE'),

-- Menu Management
('MENU_CREATE', 'Create menu items', 'MENU', 'CREATE'),
('MENU_READ', 'View menu items', 'MENU', 'READ'),
('MENU_UPDATE', 'Update menu items', 'MENU', 'UPDATE'),
('MENU_DELETE', 'Delete menu items', 'MENU', 'DELETE'),

-- Order Management
('ORDER_CREATE', 'Create orders', 'ORDER', 'CREATE'),
('ORDER_READ', 'View orders', 'ORDER', 'READ'),
('ORDER_UPDATE', 'Update order status', 'ORDER', 'UPDATE'),
('ORDER_DELETE', 'Delete/Cancel orders', 'ORDER', 'DELETE'),

-- Kitchen Management
('KITCHEN_READ', 'View kitchen orders', 'KITCHEN', 'READ'),
('KITCHEN_UPDATE', 'Update kitchen order status', 'KITCHEN', 'UPDATE'),

-- Payment Management
('PAYMENT_CREATE', 'Process payments', 'PAYMENT', 'CREATE'),
('PAYMENT_READ', 'View payment details', 'PAYMENT', 'READ'),
('PAYMENT_UPDATE', 'Update payment status', 'PAYMENT', 'UPDATE'),

-- Inventory Management
('INVENTORY_CREATE', 'Create inventory items', 'INVENTORY', 'CREATE'),
('INVENTORY_READ', 'View inventory', 'INVENTORY', 'READ'),
('INVENTORY_UPDATE', 'Update inventory', 'INVENTORY', 'UPDATE'),
('INVENTORY_DELETE', 'Delete inventory items', 'INVENTORY', 'DELETE'),

-- Reports
('REPORT_READ', 'View reports', 'REPORT', 'READ'),
('REPORT_EXPORT', 'Export reports', 'REPORT', 'EXPORT'),

-- Settings
('SETTINGS_READ', 'View settings', 'SETTINGS', 'READ'),
('SETTINGS_UPDATE', 'Update settings', 'SETTINGS', 'UPDATE');

-- ============================================
-- 5. ROLE_PERMISSIONS JUNCTION TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) 
        REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) 
        REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assign permissions to roles
-- ADMIN - all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ROLE_ADMIN';

-- OWNER - all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ROLE_OWNER';

-- MANAGER - most permissions except user deletion
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_MANAGER' 
AND p.name NOT IN ('USER_DELETE');

-- WAITER - specific permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_WAITER' 
AND p.name IN ('ORDER_CREATE', 'ORDER_READ', 'MENU_READ', 'TABLE_READ', 'TABLE_UPDATE');

-- CASHIER - payment and order related
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_CASHIER' 
AND p.name IN ('ORDER_READ', 'PAYMENT_CREATE', 'PAYMENT_READ', 'PAYMENT_UPDATE', 'REPORT_READ');

-- KITCHEN - kitchen and order related
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_KITCHEN' 
AND p.name IN ('KITCHEN_READ', 'KITCHEN_UPDATE', 'ORDER_READ');

-- CUSTOMER - minimal permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_CUSTOMER' 
AND p.name IN ('ORDER_CREATE', 'MENU_READ');

-- ============================================
-- 6. USERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id BIGINT,
    branch_id BIGINT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    profile_picture_url VARCHAR(500),
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    last_login TIMESTAMP,
    password_changed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_restaurant FOREIGN KEY (restaurant_id) 
        REFERENCES restaurants(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_branch FOREIGN KEY (branch_id) 
        REFERENCES branches(id) ON DELETE SET NULL,
    INDEX idx_username (username),
    INDEX idx_restaurant (restaurant_id),
    INDEX idx_branch (branch_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 7. USER_ROLES JUNCTION TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) 
        REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_assigned FOREIGN KEY (assigned_by) 
        REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 8. AUDIT_LOGS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    restaurant_id BIGINT,
    branch_id BIGINT,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    action VARCHAR(50),
    description VARCHAR(500),
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_restaurant FOREIGN KEY (restaurant_id) 
        REFERENCES restaurants(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_branch FOREIGN KEY (branch_id) 
        REFERENCES branches(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_restaurant (restaurant_id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 9. SAMPLE DATA FOR TESTING
-- ============================================

-- Insert sample restaurant
INSERT INTO restaurants (name, description, phone, email, address, city, country, currency_code, website)
VALUES (
    'Sumaye Restaurant',
    'A modern restaurant management system',
    '+255 123 456 789',
    'info@sumaye.com',
    'New Street 123',
    'Dar es Salaam',
    'Tanzania',
    'TZS',
    'www.sumaye.com'
);

-- Insert sample branches
INSERT INTO branches (restaurant_id, name, phone, email, address, city, opening_time, closing_time)
SELECT id, 'Downtown Branch', '+255 111 111 111', 'downtown@sumaye.com', 'Downtown Street', 'Dar es Salaam', '08:00:00', '23:00:00'
FROM restaurants WHERE name = 'Sumaye Restaurant';

INSERT INTO branches (restaurant_id, name, phone, email, address, city, opening_time, closing_time)
SELECT id, 'Airport Branch', '+255 222 222 222', 'airport@sumaye.com', 'Airport Street', 'Dar es Salaam', '06:00:00', '23:00:00'
FROM restaurants WHERE name = 'Sumaye Restaurant';

-- Insert sample admin user
-- Password: admin123 (hashed with BCrypt - generate this)
-- Raw: admin123 → BCrypt hash: $2a$10$slYQmyNdGzin7olVN3p5Be7DlH.PKZbv5H8KnzzVgXXbVxzy
INSERT INTO users (restaurant_id, branch_id, username, email, password, first_name, last_name, phone_number)
VALUES (
    (SELECT id FROM restaurants WHERE name = 'Sumaye Restaurant'),
    (SELECT id FROM branches WHERE name = 'Downtown Branch' LIMIT 1),
    'admin',
    'admin@sumaye.com',
    '$2a$10$slYQmyNdGzin7olVN3p5Be7DlH.PKZbv5H8KnzzVgXXbVxzy7y',
    'Admin',
    'User',
    '+255 700 000 001'
);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- Insert sample manager user
-- Password: manager123
INSERT INTO users (restaurant_id, branch_id, username, email, password, first_name, last_name, phone_number)
VALUES (
    (SELECT id FROM restaurants WHERE name = 'Sumaye Restaurant'),
    (SELECT id FROM branches WHERE name = 'Downtown Branch' LIMIT 1),
    'manager',
    'manager@sumaye.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lB36',
    'Manager',
    'User',
    '+255 700 000 002'
);

-- Assign MANAGER role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'manager' AND r.name = 'ROLE_MANAGER';

-- Insert sample waiter user
-- Password: waiter123
INSERT INTO users (restaurant_id, branch_id, username, email, password, first_name, last_name, phone_number)
VALUES (
    (SELECT id FROM restaurants WHERE name = 'Sumaye Restaurant'),
    (SELECT id FROM branches WHERE name = 'Downtown Branch' LIMIT 1),
    'waiter',
    'waiter@sumaye.com',
    '$2a$10$8zzBgCpqKJCH5NR9xWvSEeqHvJ8DqJsC5gDhX9KXkdE5GV2NOPZHS',
    'Waiter',
    'User',
    '+255 700 000 003'
);

-- Assign WAITER role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'waiter' AND r.name = 'ROLE_WAITER';

-- Insert sample cashier user
-- Password: cashier123
INSERT INTO users (restaurant_id, branch_id, username, email, password, first_name, last_name, phone_number)
VALUES (
    (SELECT id FROM restaurants WHERE name = 'Sumaye Restaurant'),
    (SELECT id FROM branches WHERE name = 'Downtown Branch' LIMIT 1),
    'cashier',
    'cashier@sumaye.com',
    '$2a$10$4rOZI3kDFiH7L3QT2e5zy.a5Ny.OWYBL9kWnZVIDiU7e2LSBp6Ffi',
    'Cashier',
    'User',
    '+255 700 000 004'
);

-- Assign CASHIER role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'cashier' AND r.name = 'ROLE_CASHIER';

-- Insert sample kitchen user
-- Password: kitchen123
INSERT INTO users (restaurant_id, branch_id, username, email, password, first_name, last_name, phone_number)
VALUES (
    (SELECT id FROM restaurants WHERE name = 'Sumaye Restaurant'),
    (SELECT id FROM branches WHERE name = 'Downtown Branch' LIMIT 1),
    'kitchen',
    'kitchen@sumaye.com',
    '$2a$10$GdjRf0n3cLEePdIwJ8i6y.LdwJ5fXD3IZEcUYNGOxJvUOKhiJHBxC',
    'Kitchen',
    'Staff',
    '+255 700 000 005'
);

-- Assign KITCHEN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'kitchen' AND r.name = 'ROLE_KITCHEN';

-- ============================================
-- 10. CREATE INDEXES FOR PERFORMANCE
-- ============================================
CREATE INDEX idx_users_restaurant ON users(restaurant_id);
CREATE INDEX idx_users_branch ON users(branch_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(created_at);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
CREATE INDEX idx_role_perms ON role_permissions(role_id);

-- ============================================
-- Verification Queries
-- ============================================

-- Check all roles
SELECT * FROM roles;

-- Check admin user
SELECT u.*, GROUP_CONCAT(r.name) as roles FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin'
GROUP BY u.id;

-- Check admin permissions
SELECT DISTINCT p.* FROM roles r
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE r.name = 'ROLE_ADMIN'
ORDER BY p.resource, p.action;

COMMIT;
