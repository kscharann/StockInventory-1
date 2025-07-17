# 📮 Postman Setup Guide for Invoice APIs

## 🚀 Quick Start

### 1. **Import the Collection**
1. Open Postman
2. Click **Import** button
3. Select `Invoice_API_Postman_Collection.json`
4. Collection will be imported with all endpoints

### 2. **Configure Environment (Optional)**
You can use the collection variables or create an environment:

**Collection Variables (Already Set):**
- `baseUrl`: http://localhost:8080
- `authToken`: (auto-populated after login)
- `invoiceId`: (auto-populated from responses)
- `createdInvoiceId`: (auto-populated after creating invoice)

### 3. **Start Your Application**
```bash
mvn spring-boot:run
```
Make sure your application is running on `http://localhost:8080`

---

## 🔐 Authentication Flow

### Step 1: Register User (Optional)
- **Endpoint**: `POST /api/auth/signup`
- **Purpose**: Create a new user account
- **Body**: 
```json
{
  "username": "invoiceuser",
  "email": "invoice@example.com",
  "password": "password123",
  "role": ["mod"]
}
```

### Step 2: Login
- **Endpoint**: `POST /api/auth/signin`
- **Purpose**: Get JWT authentication token
- **Body**:
```json
{
  "username": "invoiceuser",
  "password": "password123"
}
```
- **Auto-Script**: Token is automatically saved to `{{authToken}}` variable

---

## 🧾 Invoice API Endpoints

### 📋 **Read Operations**

#### 1. Get All Invoices
- **Method**: GET
- **URL**: `{{baseUrl}}/api/invoices`
- **Auth**: Bearer Token
- **Auto-Script**: Saves first invoice ID for other requests

#### 2. Get Invoice by ID
- **Method**: GET  
- **URL**: `{{baseUrl}}/api/invoices/{{invoiceId}}`
- **Auth**: Bearer Token

#### 3. Get Invoice by Invoice ID
- **Method**: GET
- **URL**: `{{baseUrl}}/api/invoices/invoice/INV-2024-001`
- **Auth**: Bearer Token

#### 4. Get Invoices by Client
- **Method**: GET
- **URL**: `{{baseUrl}}/api/invoices/client/ABC`
- **Auth**: Bearer Token

#### 5. Get Invoices by Status
- **Method**: GET
- **URL**: `{{baseUrl}}/api/invoices/status/PENDING`
- **Auth**: Bearer Token
- **Status Options**: DRAFT, PENDING, APPROVED, PAID, OVERDUE, CANCELLED

#### 6. Get Invoices by Date Range
- **Method**: GET
- **URL**: `{{baseUrl}}/api/invoices/date-range?startDate=2024-01-01&endDate=2024-12-31`
- **Auth**: Bearer Token

#### 7. Get Pending Approval Invoices
- **Method**: GET
- **URL**: `{{baseUrl}}/api/invoices/pending-approval`
- **Auth**: Bearer Token (MODERATOR/ADMIN)

#### 8. Get Total Amount by Status
- **Method**: GET
- **URL**: `{{baseUrl}}/api/invoices/total-amount/PAID`
- **Auth**: Bearer Token

### ✏️ **Write Operations**

#### 9. Create Invoice
- **Method**: POST
- **URL**: `{{baseUrl}}/api/invoices`
- **Auth**: Bearer Token (MODERATOR/ADMIN)
- **Body**:
```json
{
  "invoiceId": "INV-{{$timestamp}}",
  "clientName": "ABC Company Ltd",
  "amount": 5000.00,
  "invoiceDate": "2024-01-15",
  "paymentStatus": "DRAFT"
}
```
- **Auto-Script**: Saves created invoice ID

#### 10. Update Invoice
- **Method**: PUT
- **URL**: `{{baseUrl}}/api/invoices/{{createdInvoiceId}}`
- **Auth**: Bearer Token (MODERATOR/ADMIN)
- **Body**:
```json
{
  "invoiceId": "INV-2024-001",
  "clientName": "ABC Company Ltd",
  "amount": 5500.00,
  "invoiceDate": "2024-01-15",
  "paymentStatus": "PENDING"
}
```

#### 11. Update Payment Status
- **Method**: PATCH
- **URL**: `{{baseUrl}}/api/invoices/{{createdInvoiceId}}/status?status=PAID`
- **Auth**: Bearer Token (MODERATOR/ADMIN)

#### 12. Approve Invoice
- **Method**: PATCH
- **URL**: `{{baseUrl}}/api/invoices/{{createdInvoiceId}}/approve`
- **Auth**: Bearer Token (MODERATOR/ADMIN)

#### 13. Delete Invoice
- **Method**: DELETE
- **URL**: `{{baseUrl}}/api/invoices/{{createdInvoiceId}}`
- **Auth**: Bearer Token (ADMIN only)

---

## 🎯 **Testing Workflow**

### Recommended Testing Order:

1. **🔐 Authentication**
   - Register User (if needed)
   - Login (saves token automatically)

2. **📋 Read Empty State**
   - Get All Invoices (should return empty array)

3. **➕ Create Data**
   - Create Invoice (saves invoice ID automatically)

4. **📋 Read Operations**
   - Get All Invoices
   - Get Invoice by ID
   - Get Invoice by Invoice ID
   - Get Invoices by Status

5. **✏️ Update Operations**
   - Update Payment Status
   - Approve Invoice
   - Update Invoice

6. **🗑️ Cleanup (Optional)**
   - Delete Invoice

---

## 🔧 **Advanced Features**

### Auto-Generated Values
- **Invoice ID**: Uses `{{$timestamp}}` for unique IDs
- **Dynamic Variables**: Collection automatically manages IDs
- **Token Management**: JWT token auto-saved and used

### Test Scripts
- **Validation**: Each request includes response validation
- **Variable Management**: Auto-saves important values
- **Console Logging**: Helpful debug information

### Error Handling
- **Status Code Validation**: Tests verify correct HTTP codes
- **Response Structure**: Validates expected JSON structure
- **Authentication**: Clear error messages for auth issues

---

## 🚨 **Troubleshooting**

### Common Issues:

1. **401 Unauthorized**
   - Solution: Run Login request to get fresh token

2. **403 Forbidden**
   - Solution: Ensure user has correct role (MODERATOR/ADMIN)

3. **404 Not Found**
   - Solution: Check if invoice ID exists, run "Get All Invoices" first

4. **400 Bad Request**
   - Solution: Check request body format and required fields

5. **Connection Error**
   - Solution: Ensure Spring Boot app is running on localhost:8080

---

## 🎉 **Ready to Test!**

Your Postman collection is fully configured with:
- ✅ Authentication flow
- ✅ All 13 Invoice endpoints
- ✅ Auto-token management
- ✅ Dynamic variables
- ✅ Response validation
- ✅ Error handling

**Happy Testing! 🚀**
