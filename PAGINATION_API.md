# Pagination API Documentation

This document describes the pagination endpoints that have been added to the Finance Application.

## Overview

Pagination has been implemented for:
- **Transaction History**: Get paginated transaction history for an account
- **Account Lists**: Get paginated account lists for a user

## Transaction History Pagination

### Endpoint
```
GET /api/transactions/account/{accountId}/paginated
```

### Parameters
- `accountId` (path): The ID of the account to get transactions for
- `page` (query, optional): Page number (0-based, default: 0)
- `size` (query, optional): Number of items per page (default: 10)
- `sortBy` (query, optional): Field to sort by (default: "createdAt")
- `sortDir` (query, optional): Sort direction - "asc" or "desc" (default: "desc")

### Example Requests
```bash
# Get first page with default settings (10 transactions, sorted by creation date desc)
GET /api/transactions/account/123/paginated

# Get second page with 20 transactions per page
GET /api/transactions/account/123/paginated?page=1&size=20

# Get transactions sorted by amount ascending
GET /api/transactions/account/123/paginated?sortBy=amount&sortDir=asc

# Custom pagination and sorting
GET /api/transactions/account/123/paginated?page=2&size=15&sortBy=createdAt&sortDir=desc
```

### Response Format
```json
{
  "content": [
    {
      "id": 1,
      "sourceAccountId": 123,
      "destinationAccountId": 456,
      "type": "TRANSFER",
      "amount": 100.00,
      "description": "Payment for services",
      "status": "COMPLETED",
      "transactionRef": "TXN-ABC123-456789",
      "createdAt": "2024-01-01T10:00:00"
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "pageSize": 10,
    "pageNumber": 0,
    "paged": true,
    "unpaged": false
  },
  "last": false,
  "totalPages": 5,
  "totalElements": 50,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "first": true,
  "numberOfElements": 10,
  "empty": false
}
```

## Account Lists Pagination

### Endpoint
```
GET /api/accounts/paginated
```

### Parameters
- `page` (query, optional): Page number (0-based, default: 0)
- `size` (query, optional): Number of items per page (default: 10)
- `sortBy` (query, optional): Field to sort by (default: "createdAt")
- `sortDir` (query, optional): Sort direction - "asc" or "desc" (default: "desc")

### Example Requests
```bash
# Get first page with default settings (10 accounts, sorted by creation date desc)
GET /api/accounts/paginated

# Get second page with 5 accounts per page
GET /api/accounts/paginated?page=1&size=5

# Get accounts sorted by account number ascending
GET /api/accounts/paginated?sortBy=accountNumber&sortDir=asc

# Custom pagination and sorting
GET /api/accounts/paginated?page=0&size=20&sortBy=balance&sortDir=desc
```

### Response Format
```json
{
  "content": [
    {
      "id": 1,
      "accountNumber": "ACC123456789",
      "accountType": "SAVINGS",
      "balance": 1500.00,
      "accountStatus": "ACTIVE",
      "userId": 1,
      "createdAt": "2024-01-01T10:00:00"
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "pageSize": 10,
    "pageNumber": 0,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 3,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "first": true,
  "numberOfElements": 3,
  "empty": false
}
```

## Response Structure

### Page Metadata
- `content`: Array of items for the current page
- `totalElements`: Total number of items across all pages
- `totalPages`: Total number of pages
- `size`: Number of items per page (page size)
- `number`: Current page number (0-based)
- `first`: Boolean indicating if this is the first page
- `last`: Boolean indicating if this is the last page
- `numberOfElements`: Number of items in the current page
- `empty`: Boolean indicating if the page is empty

### Sorting Information
- `sort.sorted`: Boolean indicating if results are sorted
- `sort.empty`: Boolean indicating if no sorting is applied
- `sort.unsorted`: Boolean indicating if results are unsorted

## Authentication

All pagination endpoints require JWT authentication. Include the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Backward Compatibility

The original non-paginated endpoints are still available:
- `GET /api/transactions/account/{accountId}` - Returns all transactions (non-paginated)
- `GET /api/accounts` - Returns all accounts (non-paginated)

## Error Handling

The pagination endpoints use the same error handling as the regular endpoints:
- 401 Unauthorized: Invalid or missing JWT token
- 403 Forbidden: User doesn't have access to the requested account
- 404 Not Found: Account not found
- 400 Bad Request: Invalid pagination parameters

## Best Practices

1. **Default Page Size**: Use reasonable page sizes (10-50 items) to balance performance and usability
2. **Sorting**: Always specify a consistent sort order for predictable pagination
3. **Error Handling**: Check for empty pages and handle edge cases
4. **Performance**: Use pagination for large datasets to improve API response times
5. **Client-side**: Implement proper pagination controls in your frontend applications
