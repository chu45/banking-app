-- Add account_status column to accounts table
ALTER TABLE accounts 
ADD COLUMN account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Create index on account_status for better query performance
CREATE INDEX idx_accounts_status ON accounts(account_status);
