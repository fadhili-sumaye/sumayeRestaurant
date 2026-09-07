-- Phase 8: customer, reservation and delivery foundations. Run after Phase 7.
USE restaurant_management;

CREATE TABLE IF NOT EXISTS customers (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, restaurant_id BIGINT NOT NULL, branch_id BIGINT,
 full_name VARCHAR(150) NOT NULL, phone_number VARCHAR(20) NOT NULL, email VARCHAR(150), address VARCHAR(500), notes VARCHAR(500),
 active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NULL,
 CONSTRAINT uq_customer_restaurant_phone UNIQUE (restaurant_id, phone_number),
 FOREIGN KEY (restaurant_id) REFERENCES restaurants(id), FOREIGN KEY (branch_id) REFERENCES branches(id),
 INDEX idx_customer_search (restaurant_id, full_name), INDEX idx_customer_phone (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reservations (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, customer_id BIGINT NOT NULL, branch_id BIGINT NOT NULL, table_id BIGINT NOT NULL,
 reservation_date DATE NOT NULL, start_time TIME NOT NULL, end_time TIME NOT NULL, guest_count INT NOT NULL,
 status ENUM('PENDING','CONFIRMED','SEATED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'PENDING', notes VARCHAR(500), created_by_id BIGINT NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NULL,
 FOREIGN KEY (customer_id) REFERENCES customers(id), FOREIGN KEY (branch_id) REFERENCES branches(id), FOREIGN KEY (table_id) REFERENCES restaurant_tables(id), FOREIGN KEY (created_by_id) REFERENCES users(id),
 INDEX idx_reservation_branch_date (branch_id,reservation_date,status), INDEX idx_reservation_table_time (table_id,reservation_date,start_time,end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS delivery_orders (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL UNIQUE, customer_id BIGINT NOT NULL, branch_id BIGINT NOT NULL,
 delivery_address VARCHAR(500) NOT NULL, location_notes VARCHAR(500), delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
 status ENUM('PENDING','CONFIRMED','PREPARING','READY','ASSIGNED','OUT_FOR_DELIVERY','DELIVERED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
 rider_id BIGINT, assigned_at TIMESTAMP NULL, picked_up_at TIMESTAMP NULL, delivered_at TIMESTAMP NULL, notes VARCHAR(500), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NULL,
 FOREIGN KEY (order_id) REFERENCES orders(id), FOREIGN KEY (customer_id) REFERENCES customers(id), FOREIGN KEY (branch_id) REFERENCES branches(id), FOREIGN KEY (rider_id) REFERENCES users(id),
 INDEX idx_delivery_branch_status (branch_id,status), INDEX idx_delivery_rider_status (rider_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE bills ADD COLUMN IF NOT EXISTS delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00;

CREATE TABLE IF NOT EXISTS delivery_fee_settings (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, branch_id BIGINT NOT NULL UNIQUE,
 fixed_fee DECIMAL(10,2) NOT NULL DEFAULT 0, enabled BOOLEAN NOT NULL DEFAULT TRUE, updated_at TIMESTAMP NULL,
 FOREIGN KEY (branch_id) REFERENCES branches(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS delivery_assignments (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, delivery_order_id BIGINT NOT NULL, rider_id BIGINT NOT NULL, assigned_by_id BIGINT NULL,
 assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, picked_up_at TIMESTAMP NULL, delivered_at TIMESTAMP NULL, notes VARCHAR(500), status VARCHAR(50) NULL,
 FOREIGN KEY (delivery_order_id) REFERENCES delivery_orders(id), FOREIGN KEY (rider_id) REFERENCES users(id), FOREIGN KEY (assigned_by_id) REFERENCES users(id),
 INDEX idx_delivery_assignment_order (delivery_order_id, assigned_at), INDEX idx_delivery_assignment_rider (rider_id, assigned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
