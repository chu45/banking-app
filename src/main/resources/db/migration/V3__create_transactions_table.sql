CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_account_id BIGINT,
    destination_account_id BIGINT,
    type VARCHAR(20) CHECK (type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER')),
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_destination_account FOREIGN KEY (destination_account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

