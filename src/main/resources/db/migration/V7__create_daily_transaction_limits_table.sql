-- Create daily_transaction_limits table
CREATE TABLE daily_transaction_limits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    date DATE NOT NULL,
    daily_limit DECIMAL(15,2) NOT NULL,
    used_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    transaction_count INT NOT NULL DEFAULT 0,
    max_transaction_count INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_daily_limits_account 
        FOREIGN KEY (account_id) REFERENCES accounts(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT uk_daily_limits_account_type_date 
        UNIQUE (account_id, transaction_type, date),
    
    CONSTRAINT chk_daily_limit_positive 
        CHECK (daily_limit >= 0),
    
    CONSTRAINT chk_used_amount_positive 
        CHECK (used_amount >= 0),
    
    CONSTRAINT chk_transaction_count_positive 
        CHECK (transaction_count >= 0),
    
    CONSTRAINT chk_max_transaction_count_positive 
        CHECK (max_transaction_count > 0),
    
    CONSTRAINT chk_used_amount_within_limit 
        CHECK (used_amount <= daily_limit),
    
    CONSTRAINT chk_transaction_count_within_limit 
        CHECK (transaction_count <= max_transaction_count),
    
    INDEX idx_daily_limits_account_date (account_id, date),
    INDEX idx_daily_limits_account_type (account_id, transaction_type),
    INDEX idx_daily_limits_date (date)
);
