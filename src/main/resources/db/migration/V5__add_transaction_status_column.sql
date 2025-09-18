-- Add status column to transactions table
ALTER TABLE transactions ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED';

-- Add check constraint to ensure valid status values
ALTER TABLE transactions ADD CONSTRAINT chk_transaction_status 
    CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'));

-- Add index on status column for better query performance
CREATE INDEX idx_transactions_status ON transactions(status);
