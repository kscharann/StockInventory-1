# 📋 Purchase Management API Guide

## 🔐 Authentication Required
All Purchase APIs require JWT authentication. First, get your token:

### 1. Register User (if needed)
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "role": ["mod"]
  }'
```

### 2. Login to Get Token
```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "roles": ["ROLE_MODERATOR"]
}
```

---

## 📦 Purchase API Endpoints

### 🔍 **GET All Purchases**
```bash
curl -X GET http://localhost:8080/api/purchases \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
[
  {
    "id": 1,
    "vendorName": "ABC Suppliers",
    "poId": "PO-2024-001",
    "productName": "Laptop Dell XPS 13",
    "quantity": 5,
    "unitPrice": 1200.00,
    "totalPrice": 6000.00,
    "purchaseDate": "2024-01-15",
    "deliveryDate": "2024-01-25",
    "paymentStatus": "PENDING",
    "productId": 1,
    "productCode": "LAPTOP001",
    "createdBy": "testuser",
    "createdOn": "2024-01-15T10:30:00"
  }
]
```

### 🔍 **GET Purchase by ID**
```bash
curl -X GET http://localhost:8080/api/purchases/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🔍 **GET Purchase by PO ID**
```bash
curl -X GET http://localhost:8080/api/purchases/po/PO-2024-001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🔍 **GET Purchases by Vendor**
```bash
curl -X GET http://localhost:8080/api/purchases/vendor/ABC \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🔍 **GET Purchases by Payment Status**
```bash
# Available statuses: PENDING, PAID, PARTIAL, OVERDUE, CANCELLED
curl -X GET http://localhost:8080/api/purchases/status/PENDING \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🔍 **GET Purchases by Date Range**
```bash
curl -X GET "http://localhost:8080/api/purchases/date-range?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### ➕ **CREATE New Purchase**
```bash
curl -X POST http://localhost:8080/api/purchases \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
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

**Success Response (201 Created):**
```json
{
  "id": 1,
  "vendorName": "ABC Suppliers",
  "poId": "PO-2024-001",
  "productName": "Laptop Dell XPS 13",
  "quantity": 5,
  "unitPrice": 1200.00,
  "totalPrice": 6000.00,
  "purchaseDate": "2024-01-15",
  "deliveryDate": "2024-01-25",
  "paymentStatus": "PENDING",
  "productId": 1,
  "productCode": "LAPTOP001",
  "createdBy": "testuser",
  "createdOn": "2024-01-15T10:30:00"
}
```

### ✏️ **UPDATE Purchase**
```bash
curl -X PUT http://localhost:8080/api/purchases/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "vendorName": "XYZ Suppliers",
    "poId": "PO-2024-001",
    "productName": "Laptop Dell XPS 15",
    "quantity": 3,
    "unitPrice": 1500.00,
    "purchaseDate": "2024-01-15",
    "deliveryDate": "2024-01-20",
    "paymentStatus": "PARTIAL",
    "productId": 1
  }'
```

### 🔄 **UPDATE Payment Status Only**
```bash
curl -X PATCH "http://localhost:8080/api/purchases/1/status?status=PAID" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🗑️ **DELETE Purchase** (Admin Only)
```bash
curl -X DELETE http://localhost:8080/api/purchases/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Success Response:**
```json
{
  "message": "Purchase deleted successfully"
}
```

---

## 📝 Request/Response Models

### PurchaseRequest (for POST/PUT)
```json
{
  "vendorName": "string (required, max 100 chars)",
  "poId": "string (required, max 50 chars, unique)",
  "productName": "string (required, max 100 chars)",
  "quantity": "integer (required, positive)",
  "unitPrice": "decimal (required, positive)",
  "totalPrice": "decimal (optional, auto-calculated)",
  "purchaseDate": "date (required, YYYY-MM-DD)",
  "deliveryDate": "date (optional, YYYY-MM-DD)",
  "paymentStatus": "enum (optional, default: PENDING)",
  "productId": "long (optional, links to existing product)"
}
```

### Payment Status Values
- `PENDING` - Payment is pending
- `PAID` - Payment completed
- `PARTIAL` - Partial payment made
- `OVERDUE` - Payment is overdue
- `CANCELLED` - Purchase cancelled

---

## 🔒 Permission Requirements

| Endpoint | USER | MODERATOR | ADMIN |
|----------|------|-----------|-------|
| GET (all read operations) | ✅ | ✅ | ✅ |
| POST (create) | ❌ | ✅ | ✅ |
| PUT (update) | ❌ | ✅ | ✅ |
| PATCH (update status) | ❌ | ✅ | ✅ |
| DELETE | ❌ | ❌ | ✅ |

---

## ⚠️ Error Responses

### 400 Bad Request
```json
{
  "message": "Error: PO ID already exists!"
}
```

### 401 Unauthorized
```json
{
  "message": "Unauthorized"
}
```

### 403 Forbidden
```json
{
  "message": "Access Denied"
}
```

### 404 Not Found
```json
{
  "message": "Purchase not found"
}
```
