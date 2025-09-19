# Transaction Limits API Documentation

## Overview

This implementation adds comprehensive transaction limits to the finance application with the following features:

### 1. Daily Transaction Limits
- **Per Account**: Each account has individual daily limits
- **Per Transaction Type**: Separate limits for DEPOSIT, WITHDRAW, and TRANSFER
- **Amount Limits**: Maximum daily amount per transaction type
- **Count Limits**: Maximum number of transactions per day per type

### 2. Maximum Transaction Amount Validations
- **System-wide limits**: Configurable maximum amounts for each transaction type
- **Configured in application.yml**:
  - Max Transfer: 1,000,000.00
  - Max Deposit: 500,000.00  
  - Max Withdraw: 100,000.00

## Configuration

### Application Properties (application.yml)
```yaml
finance:
  transaction:
    # Maximum transaction limits (system-wide)
    max-transfer-limit: 1000000.00
    max-deposit-limit: 500000.00
    max-withdraw-limit: 100000.00
    
    # Default daily limits (per account, per transaction type)
    default-daily-limit: 50000.00
    default-daily-transaction-count: 50
```

### Default Daily Limits by Transaction Type
- **DEPOSIT**: 100,000.00 (2x default)
- **WITHDRAW**: 25,000.00 (0.5x default)
- **TRANSFER**: 50,000.00 (1x default)

## API Endpoints

### 1. Get Daily Limits for Account
```
GET /api/transactions/limits/{accountNumber}
Headers: Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "DEPOSIT": {
    "remaining": 95000.00
  },
  "WITHDRAW": {
    "remaining": 20000.00
  },
  "TRANSFER": {
    "remaining": 45000.00
  }
}
```

### 2. Update Daily Limits
```
PUT /api/transactions/limits/{accountNumber}?transactionType=DEPOSIT&newLimit=150000.00&newTransactionCountLimit=75
Headers: Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "message": "Daily limit updated successfully"
}
```

### 3. Transaction Operations (Updated with Limits)

All existing transaction endpoints now validate against both daily and maximum limits:

- `POST /api/transactions/deposit/{accountNumber}`
- `POST /api/transactions/withdraw/{accountNumber}`
- `POST /api/transactions/transfer/{sourceAccountNumber}/to/{destinationAccountNumber}`

## Error Responses

### Daily Limit Exceeded
```json
{
  "error": "Transaction amount 60000.00 exceeds daily remaining limit of 45000.00 for TRANSFER transactions"
}
```

### Maximum Limit Exceeded
```json
{
  "error": "Transfer amount 1500000.00 exceeds maximum allowed limit of 1000000.00"
}
```

### Transaction Count Limit Exceeded
```json
{
  "error": "Daily transaction count limit exceeded. Maximum 50 transactions allowed per day for DEPOSIT transactions"
}
```

## Database Schema

### Daily Transaction Limits Table
```sql
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
        UNIQUE (account_id, transaction_type, date)
);
```

## Implementation Features

### 1. Automatic Limit Creation
- Daily limits are automatically created for each account when first transaction is attempted
- Uses configurable default values from application properties

### 2. Real-time Tracking
- Each successful transaction updates the daily limit usage
- Failed transactions do not count against limits

### 3. Validation Flow
1. **Maximum Amount Check**: Validates against system-wide maximum limits
2. **Daily Limit Check**: Validates against account's daily remaining limit
3. **Transaction Count Check**: Validates against daily transaction count limit
4. **Transaction Processing**: Only proceeds if all validations pass
5. **Limit Recording**: Updates daily usage after successful transaction

### 4. Exception Handling
- Custom exceptions for different limit violations
- Proper HTTP status codes and error messages
- Global exception handler integration

### 5. Performance Considerations
- Efficient database queries with proper indexing
- Transactional consistency for limit updates
- Optimistic approach - validate before processing

## Usage Examples

### Example 1: Successful Transaction Within Limits
```bash
curl -X POST "http://localhost:8080/api/transactions/deposit/ACC123456789" \
  -H "Authorization: Bearer your_jwt_token" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000.00,
    "description": "Salary deposit"
  }'
```

### Example 2: Transaction Exceeding Daily Limit
```bash
curl -X POST "http://localhost:8080/api/transactions/transfer/ACC123456789/to/ACC987654321" \
  -H "Authorization: Bearer your_jwt_token" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 75000.00,
    "description": "Large transfer"
  }'
```
Response: 400 Bad Request with daily limit exceeded error

### Example 3: Check Remaining Limits
```bash
curl -X GET "http://localhost:8080/api/transactions/limits/ACC123456789" \
  -H "Authorization: Bearer your_jwt_token"
```

## Migration Notes

- Run the migration V7__create_daily_transaction_limits_table.sql
- Existing transactions are not affected
- Limits apply only to new transactions after implementation
- Default limits are conservative and can be adjusted per business requirements
