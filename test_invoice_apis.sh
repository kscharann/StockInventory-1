#!/bin/bash

# Invoice API Testing Script
# Make sure your Spring Boot application is running on localhost:8080

BASE_URL="http://localhost:8080"
echo "🧾 Testing Invoice APIs for Stock Inventory System"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
    fi
}

# Step 1: Register a test user (if needed)
echo -e "\n${BLUE}📝 Step 1: Registering test user...${NC}"
REGISTER_RESPONSE=$(curl -s -w "%{http_code}" -X POST $BASE_URL/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "invoiceuser",
    "email": "invoice@example.com", 
    "password": "password123",
    "role": ["mod"]
  }')

HTTP_CODE="${REGISTER_RESPONSE: -3}"
if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 400 ]; then
    print_status 0 "User registration (200 = success, 400 = user exists)"
else
    print_status 1 "User registration failed with code: $HTTP_CODE"
fi

# Step 2: Login to get JWT token
echo -e "\n${BLUE}🔐 Step 2: Logging in to get JWT token...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "invoiceuser",
    "password": "password123"
  }')

# Extract token from response
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    print_status 0 "Login successful, token obtained"
    echo "Token: ${TOKEN:0:50}..."
else
    print_status 1 "Login failed"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

# Step 3: Test GET all invoices (should be empty initially)
echo -e "\n${BLUE}🧾 Step 3: Getting all invoices...${NC}"
GET_ALL_RESPONSE=$(curl -s -w "%{http_code}" -X GET $BASE_URL/api/invoices \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE="${GET_ALL_RESPONSE: -3}"
RESPONSE_BODY="${GET_ALL_RESPONSE%???}"

if [ "$HTTP_CODE" -eq 200 ]; then
    print_status 0 "GET all invoices successful"
    echo "Response: $RESPONSE_BODY"
else
    print_status 1 "GET all invoices failed with code: $HTTP_CODE"
fi

# Step 4: Create a new invoice
echo -e "\n${BLUE}➕ Step 4: Creating a new invoice...${NC}"
CREATE_RESPONSE=$(curl -s -w "%{http_code}" -X POST $BASE_URL/api/invoices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "invoiceId": "INV-2024-TEST-001",
    "clientName": "Test Client Ltd",
    "amount": 2500.00,
    "invoiceDate": "2024-01-15",
    "paymentStatus": "DRAFT"
  }')

HTTP_CODE="${CREATE_RESPONSE: -3}"
RESPONSE_BODY="${CREATE_RESPONSE%???}"

if [ "$HTTP_CODE" -eq 201 ]; then
    print_status 0 "CREATE invoice successful"
    echo "Response: $RESPONSE_BODY"
    
    # Extract invoice ID for further tests
    INVOICE_ID=$(echo $RESPONSE_BODY | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "Created Invoice ID: $INVOICE_ID"
else
    print_status 1 "CREATE invoice failed with code: $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
fi

# Step 5: Get invoice by ID
if [ -n "$INVOICE_ID" ]; then
    echo -e "\n${BLUE}🔍 Step 5: Getting invoice by ID...${NC}"
    GET_BY_ID_RESPONSE=$(curl -s -w "%{http_code}" -X GET $BASE_URL/api/invoices/$INVOICE_ID \
      -H "Authorization: Bearer $TOKEN")
    
    HTTP_CODE="${GET_BY_ID_RESPONSE: -3}"
    RESPONSE_BODY="${GET_BY_ID_RESPONSE%???}"
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        print_status 0 "GET invoice by ID successful"
        echo "Response: $RESPONSE_BODY"
    else
        print_status 1 "GET invoice by ID failed with code: $HTTP_CODE"
    fi
fi

# Step 6: Get invoice by Invoice ID
echo -e "\n${BLUE}🔍 Step 6: Getting invoice by Invoice ID...${NC}"
GET_BY_INVOICE_ID_RESPONSE=$(curl -s -w "%{http_code}" -X GET $BASE_URL/api/invoices/invoice/INV-2024-TEST-001 \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE="${GET_BY_INVOICE_ID_RESPONSE: -3}"
RESPONSE_BODY="${GET_BY_INVOICE_ID_RESPONSE%???}"

if [ "$HTTP_CODE" -eq 200 ]; then
    print_status 0 "GET invoice by Invoice ID successful"
    echo "Response: $RESPONSE_BODY"
else
    print_status 1 "GET invoice by Invoice ID failed with code: $HTTP_CODE"
fi

# Step 7: Get invoices by status
echo -e "\n${BLUE}🔍 Step 7: Getting invoices by status (DRAFT)...${NC}"
GET_BY_STATUS_RESPONSE=$(curl -s -w "%{http_code}" -X GET $BASE_URL/api/invoices/status/DRAFT \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE="${GET_BY_STATUS_RESPONSE: -3}"
RESPONSE_BODY="${GET_BY_STATUS_RESPONSE%???}"

if [ "$HTTP_CODE" -eq 200 ]; then
    print_status 0 "GET invoices by status successful"
    echo "Response: $RESPONSE_BODY"
else
    print_status 1 "GET invoices by status failed with code: $HTTP_CODE"
fi

# Step 8: Update payment status to PENDING
if [ -n "$INVOICE_ID" ]; then
    echo -e "\n${BLUE}🔄 Step 8: Updating payment status to PENDING...${NC}"
    UPDATE_STATUS_RESPONSE=$(curl -s -w "%{http_code}" -X PATCH "$BASE_URL/api/invoices/$INVOICE_ID/status?status=PENDING" \
      -H "Authorization: Bearer $TOKEN")
    
    HTTP_CODE="${UPDATE_STATUS_RESPONSE: -3}"
    RESPONSE_BODY="${UPDATE_STATUS_RESPONSE%???}"
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        print_status 0 "UPDATE payment status successful"
        echo "Response: $RESPONSE_BODY"
    else
        print_status 1 "UPDATE payment status failed with code: $HTTP_CODE"
    fi
fi

# Step 9: Approve invoice
if [ -n "$INVOICE_ID" ]; then
    echo -e "\n${BLUE}✅ Step 9: Approving invoice...${NC}"
    APPROVE_RESPONSE=$(curl -s -w "%{http_code}" -X PATCH $BASE_URL/api/invoices/$INVOICE_ID/approve \
      -H "Authorization: Bearer $TOKEN")
    
    HTTP_CODE="${APPROVE_RESPONSE: -3}"
    RESPONSE_BODY="${APPROVE_RESPONSE%???}"
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        print_status 0 "APPROVE invoice successful"
        echo "Response: $RESPONSE_BODY"
    else
        print_status 1 "APPROVE invoice failed with code: $HTTP_CODE"
    fi
fi

# Step 10: Get total amount by status
echo -e "\n${BLUE}💰 Step 10: Getting total amount for APPROVED status...${NC}"
TOTAL_AMOUNT_RESPONSE=$(curl -s -w "%{http_code}" -X GET $BASE_URL/api/invoices/total-amount/APPROVED \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE="${TOTAL_AMOUNT_RESPONSE: -3}"
RESPONSE_BODY="${TOTAL_AMOUNT_RESPONSE%???}"

if [ "$HTTP_CODE" -eq 200 ]; then
    print_status 0 "GET total amount successful"
    echo "Response: $RESPONSE_BODY"
else
    print_status 1 "GET total amount failed with code: $HTTP_CODE"
fi

echo -e "\n${YELLOW}🎉 Invoice API testing completed!${NC}"
echo -e "${YELLOW}📖 Check INVOICE_API_GUIDE.md for detailed API documentation${NC}"
echo -e "${YELLOW}📮 Import Invoice_API_Postman_Collection.json into Postman for GUI testing${NC}"
