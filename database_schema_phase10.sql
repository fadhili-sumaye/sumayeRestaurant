-- Phase 10: secure, opaque QR table ordering. Run after Phase 9.
USE restaurant_management;

ALTER TABLE restaurant_tables
    ADD COLUMN IF NOT EXISTS qr_token VARCHAR(64) NULL,
    ADD UNIQUE INDEX IF NOT EXISTS uq_restaurant_table_qr_token (qr_token);

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS order_source VARCHAR(20) NOT NULL DEFAULT 'WAITER',
    ADD COLUMN IF NOT EXISTS customer_session_id VARCHAR(64) NULL,
    ADD INDEX IF NOT EXISTS idx_order_source_created (order_source, created_at),
    ADD INDEX IF NOT EXISTS idx_order_customer_session (customer_session_id),
    MODIFY COLUMN waiter_id BIGINT NULL;
