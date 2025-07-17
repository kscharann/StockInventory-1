# Purchase Management API Documentation

## Overview
The Purchase Management system allows you to track purchases with vendor information, purchase orders, product details, pricing, and payment status.

## Authentication
All endpoints require authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Purchase Endpoints

### 1. Get All Purchases
- **GET** `/api/purchases`
- **Authorization**: USER, MODERATOR, ADMIN
- **Response**: Array of PurchaseResponse objects

### 2. Get Purchase by ID
- **GET** `/api/purchases/{id}`
- **Authorization**: USER, MODERATOR, ADMIN
- **Response**: PurchaseResponse object

### 3. Get Purchase by PO ID
- **GET** `/api/purchases/po/{poId}`
- **Authorization**: USER, MODERATOR, ADMIN
- **Response**: PurchaseResponse object

### 4. Get Purchases by Vendor
- **GET** `/api/purchases/vendor/{vendorName}`
- **Authorization**: USER, MODERATOR, ADMIN
- **Response**: Array of PurchaseResponse objects

### 5. Get Purchases by Payment Status
- **GET** `/api/purchases/status/{status}`
- **Authorization**: USER, MODERATOR, ADMIN
- **Status values**: PENDING, PAID, PARTIAL, OVERDUE, CANCELLED
- **Response**: Array of PurchaseResponse objects

### 6. Get Purchases by Date Range
- **GET** `/api/purchases/date-range?startDate={startDate}&endDate={endDate}`
- **Authorization**: USER, MODERATOR, ADMIN
- **Date format**: YYYY-MM-DD
- **Response**: Array of PurchaseResponse objects

### 7. Create Purchase
- **POST** `/api/purchases`
- **Authorization**: MODERATOR, ADMIN
- **Request Body**: PurchaseRequest object
- **Response**: PurchaseResponse object

### 8. Update Purchase
- **PUT** `/api/purchases/{id}`
- **Authorization**: MODERATOR, ADMIN
- **Request Body**: PurchaseRequest object
- **Response**: PurchaseResponse object

### 9. Update Payment Status
- **PATCH** `/api/purchases/{id}/status?status={status}`
- **Authorization**: MODERATOR, ADMIN
- **Response**: PurchaseResponse object

### 10. Delete Purchase
- **DELETE** `/api/purchases/{id}`
- **Authorization**: ADMIN
- **Response**: MessageResponse object

## Data Models

### PurchaseRequest
```json
{
  "vendorName": "string (required, max 100 chars)",
  "poId": "string (required, max 50 chars, unique)",
  "productName": "string (required, max 100 chars)",
  "quantity": "integer (required, positive)",
  "unitPrice": "decimal (required, positive)",
  "totalPrice": "decimal (optional, auto-calculated if not provided)",
  "purchaseDate": "date (required, format: YYYY-MM-DD)",
  "deliveryDate": "date (optional, format: YYYY-MM-DD)",
  "paymentStatus": "enum (optional, default: PENDING)",
  "productId": "long (optional, links to existing product)"
}
```

### PurchaseResponse
```json
{
  "id": "long",
  "vendorName": "string",
  "poId": "string",
  "productName": "string",
  "quantity": "integer",
  "unitPrice": "decimal",
  "totalPrice": "decimal",
  "purchaseDate": "date",
  "deliveryDate": "date",
  "paymentStatus": "enum",
  "productId": "long",
  "productCode": "string",
  "createdBy": "string",
  "createdOn": "datetime"
}
```

### Payment Status Values
- `PENDING`: Payment is pending
- `PAID`: Payment completed
- `PARTIAL`: Partial payment made
- `OVERDUE`: Payment is overdue
- `CANCELLED`: Purchase cancelled

## Example Usage

### Create a Purchase
```bash
curl -X POST http://localhost:8080/api/purchases \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "vendorName": "ABC Suppliers",
    "poId": "PO-2024-001",
    "productName": "Laptop Dell XPS 13",
    "quantity": 5,
    "unitPrice": 1200.00,
    "purchaseDate": "2024-01-15",
    "deliveryDate": "2024-01-25",
    "paymentStatus": "PENDING",
    "productId": 1
  }'
```

### Update Payment Status
```bash
curl -X PATCH http://localhost:8080/api/purchases/1/status?status=PAID \
  -H "Authorization: Bearer <token>"
```

### Get Purchases by Date Range
```bash
curl -X GET "http://localhost:8080/api/purchases/date-range?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer <token>"
```
