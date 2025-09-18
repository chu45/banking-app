-- Add transaction_ref column to transactions table
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'finance' 
                   AND TABLE_NAME = 'transactions' 
                   AND COLUMN_NAME = 'transaction_ref');

SET @sql = IF(@col_exists = 0, 
              'ALTER TABLE transactions ADD COLUMN transaction_ref VARCHAR(50) UNIQUE COMMENT ''Unique reference ID for transaction tracking and history''', 
              'SELECT ''Column transaction_ref already exists'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index on transaction_ref for better query performance
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
                     WHERE TABLE_SCHEMA = 'finance' 
                     AND TABLE_NAME = 'transactions' 
                     AND INDEX_NAME = 'idx_transactions_ref');

SET @sql = IF(@index_exists = 0, 
              'CREATE INDEX idx_transactions_ref ON transactions(transaction_ref)', 
              'SELECT ''Index idx_transactions_ref already exists'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
