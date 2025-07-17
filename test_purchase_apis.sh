#!/bin/bash

# Purchase API Testing Script
# Make sure your Spring Boot application is running on localhost:8080

BASE_URL="http://localhost:8080"
echo "🚀 Testing Purchase APIs for Stock Inventory System"
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

# Step 1: Register a test user
echo -e "\n${BLUE}📝 Step 1: Registering test user...${NC}"
REGISTER_RESPONSE=$(curl -s -w "%{http_code}" -X POST $BASE_URL/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com", 
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
    "username": "testuser",
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

# Step 3: Test GET all purchases (should be empty initially)
echo -e "\n${BLUE}📋 Step 3: Getting all purchases...${NC}"
GET_ALL_RESPONSE=$(curl -s -w "%{http_code}" -X GET $BASE_URL/api/purchases \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE="${GET_ALL_RESPONSE: -3}"
RESPONSE_BODY="${GET_ALL_RESPONSE%???}"

if [ "$HTTP_CODE" -eq 200 ]; then
    print_status 0 "GET all purchases successful"
    echo "Response: $RESPONSE_BODY"
else
    print_status 1 "GET all purchases failed with code: $HTTP_CODE"
fi

# Step 4: Create a new purchase
echo -e "\n${BLUE}➕ Step 4: Creating a new purchase...${NC}"
CREATE_RESPONSE=$(curl -s -w "%{http_code}" -X POST $BASE_URL/api/purchases \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "vendorName": "ABC Suppliers",
    "poId": "PO-2024-TEST-001",
    "productName": "Test Laptop",
    "quantity": 2,
    "unitPrice": 1000.00,
    "purchaseDate": "2024-01-15",
    "deliveryDate": "2024-01-25",
    "paymentStatus": "PENDING"
  }')

HTTP_CODE="${CREATE_RESPONSE: -3}"
RESPONSE_BODY="${CREATE_RESPONSE%???}"

if [ "$HTTP_CODE" -eq 201 ]; then
    print_status 0 "CREATE purchase successful"
    echo "Response: $RESPONSE_BODY"
    
    # Extract purchase ID for further tests
    PURCHASE_ID=$(echo $RESPONSE_BODY | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "Created Purchase ID: $PURCHASE_ID"
else
    print_status 1 "CREATE purchase failed with code: $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
fi

# Step 5: Get purchase by ID
if [ -n "$PURCHASE_ID" ]; then
    echo -e "\n${BLUE}🔍 Step 5: Getting purchase by ID...${NC}"
    GET_BY_ID_RESPONSE=$(curl -s -w "%{http_code}" -X GET $BASE_URL/api/purchases/$PURCHASE_ID \
      -H "Authorization: Bearer $TOKEN")
    
    HTTP_CODE="${GET_BY_ID_RESPONSE: -3}"
    RESPONSE_BODY="${GET_BY_ID_RESPONSE%???}"
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        print_status 0 "GET purchase by ID successful"
        echo "Response: $RESPONSE_BODY"
    else
        print_status 1 "GET purchase by ID failed with code: $HTTP_CODE"
    fi
fi

# Step 6: Get purchase by PO ID
echo -e "\n${BLUE}🔍 Step 6: Getting purchase by PO ID...${NC}"
GET_BY_PO_RESPONSE=$(curl -s -w "%{http_code}" -X GET $BASE_URL/api/purchases/po/PO-2024-TEST-001 \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE="${GET_BY_PO_RESPONSE: -3}"
RESPONSE_BODY="${GET_BY_PO_RESPONSE%???}"

if [ "$HTTP_CODE" -eq 200 ]; then
    print_status 0 "GET purchase by PO ID successful"
    echo "Response: $RESPONSE_BODY"
else
    print_status 1 "GET purchase by PO ID failed with code: $HTTP_CODE"
fi

# Step 7: Get purchases by status
echo -e "\n${BLUE}🔍 Step 7: Getting purchases by status (PENDING)...${NC}"
GET_BY_STATUS_RESPONSE=$(curl -s -w "%{http_code}" -X GET $BASE_URL/api/purchases/status/PENDING \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE="${GET_BY_STATUS_RESPONSE: -3}"
RESPONSE_BODY="${GET_BY_STATUS_RESPONSE%???}"

if [ "$HTTP_CODE" -eq 200 ]; then
    print_status 0 "GET purchases by status successful"
    echo "Response: $RESPONSE_BODY"
else
    print_status 1 "GET purchases by status failed with code: $HTTP_CODE"
fi

# Step 8: Update payment status
if [ -n "$PURCHASE_ID" ]; then
    echo -e "\n${BLUE}🔄 Step 8: Updating payment status to PAID...${NC}"
    UPDATE_STATUS_RESPONSE=$(curl -s -w "%{http_code}" -X PATCH "$BASE_URL/api/purchases/$PURCHASE_ID/status?status=PAID" \
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

echo -e "\n${YELLOW}🎉 Purchase API testing completed!${NC}"
echo -e "${YELLOW}📖 Check PURCHASE_API_GUIDE.md for detailed API documentation${NC}"
echo -e "${YELLOW}📮 Import Purchase_API_Postman_Collection.json into Postman for GUI testing${NC}"
