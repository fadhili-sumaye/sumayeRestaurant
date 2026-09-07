-- Phase 7: inventory, recipes, suppliers and auditable stock movements.
-- Run after database_schema_phase6.sql.  The statements are safe on a new database.
USE restaurant_management;

CREATE TABLE IF NOT EXISTS ingredients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL UNIQUE,
    kiswahili_name VARCHAR(150),
    category VARCHAR(100),
    description VARCHAR(500),
    default_unit ENUM('KG','GRAM','LITRE','ML','PIECE','BOTTLE','PACKET','BOX') NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    INDEX idx_ingredients_active_category (active, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version BIGINT NOT NULL DEFAULT 0,
    branch_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity_on_hand DECIMAL(14,4) NOT NULL DEFAULT 0,
    unit ENUM('KG','GRAM','LITRE','ML','PIECE','BOTTLE','PACKET','BOX') NOT NULL,
    minimum_stock_level DECIMAL(14,4) NOT NULL DEFAULT 0,
    reorder_level DECIMAL(14,4) NOT NULL DEFAULT 0,
    maximum_stock_level DECIMAL(14,4) NULL,
    cost_per_unit DECIMAL(10,2) NOT NULL DEFAULT 0,
    last_restocked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    CONSTRAINT uq_stock_branch_ingredient UNIQUE (branch_id, ingredient_id),
    CONSTRAINT fk_stock_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_stock_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(id),
    INDEX idx_stock_branch_quantity (branch_id, quantity_on_hand)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    branch_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    transaction_type ENUM('PURCHASE','SALE_CONSUMPTION','WASTAGE','ADJUSTMENT_IN','ADJUSTMENT_OUT','RETURN','TRANSFER_IN','TRANSFER_OUT','REVERSAL') NOT NULL,
    quantity_change DECIMAL(14,4) NOT NULL,
    quantity_before DECIMAL(14,4) NOT NULL,
    quantity_after DECIMAL(14,4) NOT NULL,
    unit ENUM('KG','GRAM','LITRE','ML','PIECE','BOTTLE','PACKET','BOX') NOT NULL,
    reference_type VARCHAR(30),
    reference_id BIGINT,
    notes VARCHAR(500),
    created_by_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_tx_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_inventory_tx_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(id),
    CONSTRAINT fk_inventory_tx_user FOREIGN KEY (created_by_id) REFERENCES users(id),
    INDEX idx_inventory_tx_branch_date (branch_id, created_at),
    INDEX idx_inventory_tx_reference (reference_type, reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Recipes, suppliers, purchases, purchase items and wastage are represented by
-- the matching Phase 7 JPA entities.  Keep this migration alongside Hibernate's
-- schema update until a versioned migration runner is introduced.
