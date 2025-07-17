# 📋 Invoice Management API Guide

## 🔐 Authentication Required
All Invoice APIs require JWT authentication. Get your token from `/api/auth/signin`.

---

## 📦 Invoice API Endpoints

### 🔍 **GET All Invoices**
```bash
curl -X GET http://localhost:8080/api/invoices \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
[
  {
    "id": 1,
    "invoiceId": "10001",
    "clientName": "ABC Company Ltd",
    "amount": 5000.00,
    "invoiceDate": "2024-01-15",
    "description": "Professional services for Q1 2024 project development and consultation",
    "paymentStatus": "PENDING",
    "createdBy": "testuser",
    "approvedBy": "manager",
    "createdOn": "2024-01-15T10:30:00",
    "approvedOn": "2024-01-16T14:20:00"
  }
]
```

### 🔍 **GET Invoice by ID**
```bash
curl -X GET http://localhost:8080/api/invoices/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🔍 **GET Invoice by Invoice ID**
```bash
curl -X GET http://localhost:8080/api/invoices/invoice/INV-2024-001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🔍 **GET Invoices by Client**
```bash
curl -X GET http://localhost:8080/api/invoices/client/ABC \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🔍 **GET Invoices by Payment Status**
```bash
# Available statuses: DRAFT, PENDING, APPROVED, PAID, OVERDUE, CANCELLED
curl -X GET http://localhost:8080/api/invoices/status/PENDING \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🔍 **GET Invoices by Date Range**
```bash
curl -X GET "http://localhost:8080/api/invoices/date-range?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🔍 **GET Pending Approval Invoices**
```bash
curl -X GET http://localhost:8080/api/invoices/pending-approval \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🔍 **GET Total Amount by Status**
```bash
curl -X GET http://localhost:8080/api/invoices/total-amount/PAID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### ➕ **CREATE New Invoice**
```bash
curl -X POST http://localhost:8080/api/invoices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "invoiceId": "10001",
    "clientName": "ABC Company Ltd",
    "amount": 5000.00,
    "invoiceDate": "2024-01-15",
    "description": "Professional services for Q1 2024 project development and consultation",
    "paymentStatus": "DRAFT"
  }'
```

### ✏️ **UPDATE Invoice**
```bash
curl -X PUT http://localhost:8080/api/invoices/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "invoiceId": "INV-2024-001",
    "clientName": "ABC Company Ltd",
    "amount": 5500.00,
    "invoiceDate": "2024-01-15",
    "paymentStatus": "PENDING"
  }'
```

### 🔄 **UPDATE Payment Status**
```bash
curl -X PATCH "http://localhost:8080/api/invoices/1/status?status=PAID" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### ✅ **APPROVE Invoice**
```bash
curl -X PATCH http://localhost:8080/api/invoices/1/approve \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🗑️ **DELETE Invoice** (Admin Only)
```bash
curl -X DELETE http://localhost:8080/api/invoices/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📝 Request/Response Models

### InvoiceRequest (for POST/PUT)
```json
{
  "invoiceId": "string (required, max 50 chars, unique)",
  "clientName": "string (required, max 100 chars)",
  "amount": "decimal (required, positive)",
  "invoiceDate": "date (required, YYYY-MM-DD)",
  "description": "string (optional, max 500 chars)",
  "paymentStatus": "enum (optional, default: DRAFT)"
}
```

### InvoiceResponse
```json
{
  "id": "long",
  "invoiceId": "string",
  "clientName": "string",
  "amount": "decimal",
  "invoiceDate": "date",
  "description": "string",
  "paymentStatus": "enum",
  "createdBy": "string",
  "approvedBy": "string",
  "createdOn": "datetime",
  "approvedOn": "datetime"
}
```

### Payment Status Values
- `DRAFT` - Invoice is in draft state
- `PENDING` - Invoice is pending approval
- `APPROVED` - Invoice has been approved
- `PAID` - Invoice has been paid
- `OVERDUE` - Invoice payment is overdue
- `CANCELLED` - Invoice has been cancelled

---

## 🔒 Permission Requirements

| Endpoint | USER | MODERATOR | ADMIN |
|----------|------|-----------|-------|
| GET (all read operations) | ✅ | ✅ | ✅ |
| POST (create) | ❌ | ✅ | ✅ |
| PUT (update) | ❌ | ✅ | ✅ |
| PATCH (update status/approve) | ❌ | ✅ | ✅ |
| DELETE | ❌ | ❌ | ✅ |

---

## 🎯 **Key Features**

### ✅ **All Requested Fields Implemented:**
- ✅ Invoice ID (unique identifier)
- ✅ Client Name
- ✅ Amount
- ✅ Invoice Date
- ✅ Description (invoice details, max 500 chars)
- ✅ Created By (user tracking)
- ✅ Approved By (approval workflow)
- ✅ Payment Status (6 states)

### ✅ **Advanced Features:**
- ✅ Approval workflow with approval tracking
- ✅ Search by client, status, date range
- ✅ Pending approval list for managers
- ✅ Total amount calculations by status
- ✅ Comprehensive validation
- ✅ Role-based access control

---

## ⚠️ Error Responses

### 400 Bad Request
```json
{
  "message": "Error: Invoice ID already exists!"
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
  "message": "Invoice not found"
}
```
