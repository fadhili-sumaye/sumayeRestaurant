-- Phase 9: expenses and estimated profit/loss data.
USE restaurant_management;
CREATE TABLE IF NOT EXISTS expense_categories (id BIGINT PRIMARY KEY AUTO_INCREMENT,branch_id BIGINT NOT NULL,name VARCHAR(100) NOT NULL,description VARCHAR(300),active BOOLEAN NOT NULL DEFAULT TRUE,created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMP NULL,UNIQUE KEY uq_expense_category_branch_name(branch_id,name),FOREIGN KEY(branch_id) REFERENCES branches(id));
CREATE TABLE IF NOT EXISTS expenses (id BIGINT PRIMARY KEY AUTO_INCREMENT,branch_id BIGINT NOT NULL,expense_category_id BIGINT NOT NULL,description VARCHAR(300) NOT NULL,amount DECIMAL(14,2) NOT NULL,payment_method VARCHAR(30) NOT NULL,reference_number VARCHAR(100),expense_date DATE NOT NULL,recorded_by_id BIGINT NOT NULL,notes VARCHAR(500),status ENUM('POSTED','VOIDED') NOT NULL DEFAULT 'POSTED',void_reason VARCHAR(500),voided_by_id BIGINT,voided_at TIMESTAMP NULL,created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMP NULL,FOREIGN KEY(branch_id) REFERENCES branches(id),FOREIGN KEY(expense_category_id) REFERENCES expense_categories(id),FOREIGN KEY(recorded_by_id) REFERENCES users(id),FOREIGN KEY(voided_by_id) REFERENCES users(id),INDEX idx_expense_branch_date(branch_id,expense_date));
CREATE TABLE IF NOT EXISTS daily_closings (id BIGINT PRIMARY KEY AUTO_INCREMENT,branch_id BIGINT NOT NULL,business_date DATE NOT NULL,total_sales DECIMAL(14,2) NOT NULL DEFAULT 0,cash_sales DECIMAL(14,2) NOT NULL DEFAULT 0,mobile_money_sales DECIMAL(14,2) NOT NULL DEFAULT 0,card_sales DECIMAL(14,2) NOT NULL DEFAULT 0,bank_sales DECIMAL(14,2) NOT NULL DEFAULT 0,other_sales DECIMAL(14,2) NOT NULL DEFAULT 0,discounts DECIMAL(14,2) NOT NULL DEFAULT 0,expenses DECIMAL(14,2) NOT NULL DEFAULT 0,expected_cash DECIMAL(14,2) NOT NULL DEFAULT 0,closed_by_id BIGINT NOT NULL,closed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,notes VARCHAR(500),UNIQUE KEY uq_closing_branch_date(branch_id,business_date),FOREIGN KEY(branch_id) REFERENCES branches(id),FOREIGN KEY(closed_by_id) REFERENCES users(id));
CREATE TABLE IF NOT EXISTS cash_reconciliations (id BIGINT PRIMARY KEY AUTO_INCREMENT,daily_closing_id BIGINT NOT NULL UNIQUE,expected_cash DECIMAL(14,2) NOT NULL,actual_cash DECIMAL(14,2) NOT NULL,difference DECIMAL(14,2) NOT NULL,difference_reason VARCHAR(500),recorded_by_id BIGINT NOT NULL,created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(daily_closing_id) REFERENCES daily_closings(id),FOREIGN KEY(recorded_by_id) REFERENCES users(id));

-- Ensure a new branch can record its first expense immediately. INSERT IGNORE also
-- makes this safe to apply more than once and preserves branch-specific additions.
INSERT IGNORE INTO expense_categories (branch_id, name, description)
SELECT b.id, defaults.name, defaults.description
FROM branches b
CROSS JOIN (
    SELECT 'Rent' AS name, 'Kodi ya eneo au jengo' AS description
    UNION ALL SELECT 'Utilities', 'Maji, umeme na mawasiliano'
    UNION ALL SELECT 'Supplies', 'Vifaa vya uendeshaji'
    UNION ALL SELECT 'Transport', 'Usafiri na mafuta'
    UNION ALL SELECT 'Maintenance', 'Matengenezo na ukarabati'
    UNION ALL SELECT 'Salaries', 'Mishahara na malipo ya wafanyakazi'
    UNION ALL SELECT 'Other', 'Gharama nyingine za uendeshaji'
) AS defaults;
