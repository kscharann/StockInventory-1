@echo off
REM Purchase API Testing Script for Windows
REM Make sure your Spring Boot application is running on localhost:8080

set BASE_URL=http://localhost:8080
echo 🚀 Testing Purchase APIs for Stock Inventory System
echo ==================================================

echo.
echo 📝 Step 1: Registering test user...
curl -s -X POST %BASE_URL%/api/auth/signup ^
  -H "Content-Type: application/json" ^
  -d "{\"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\", \"role\": [\"mod\"]}"

echo.
echo 🔐 Step 2: Logging in to get JWT token...
curl -s -X POST %BASE_URL%/api/auth/signin ^
  -H "Content-Type: application/json" ^
  -d "{\"username\": \"testuser\", \"password\": \"password123\"}" > login_response.json

echo Please copy the token from login_response.json and set it as TOKEN variable
echo Then run the following commands manually:

echo.
echo 📋 Step 3: Get all purchases
echo curl -X GET %BASE_URL%/api/purchases -H "Authorization: Bearer YOUR_TOKEN"

echo.
echo ➕ Step 4: Create a new purchase
echo curl -X POST %BASE_URL%/api/purchases ^
echo   -H "Content-Type: application/json" ^
echo   -H "Authorization: Bearer YOUR_TOKEN" ^
echo   -d "{\"vendorName\": \"ABC Suppliers\", \"poId\": \"PO-2024-TEST-001\", \"productName\": \"Test Laptop\", \"quantity\": 2, \"unitPrice\": 1000.00, \"purchaseDate\": \"2024-01-15\", \"deliveryDate\": \"2024-01-25\", \"paymentStatus\": \"PENDING\"}"

echo.
echo 🔍 Step 5: Get purchase by ID (replace 1 with actual ID)
echo curl -X GET %BASE_URL%/api/purchases/1 -H "Authorization: Bearer YOUR_TOKEN"

echo.
echo 🔍 Step 6: Get purchase by PO ID
echo curl -X GET %BASE_URL%/api/purchases/po/PO-2024-TEST-001 -H "Authorization: Bearer YOUR_TOKEN"

echo.
echo 🔍 Step 7: Get purchases by status
echo curl -X GET %BASE_URL%/api/purchases/status/PENDING -H "Authorization: Bearer YOUR_TOKEN"

echo.
echo 🔄 Step 8: Update payment status (replace 1 with actual ID)
echo curl -X PATCH "%BASE_URL%/api/purchases/1/status?status=PAID" -H "Authorization: Bearer YOUR_TOKEN"

echo.
echo 🎉 Purchase API testing commands generated!
echo 📖 Check PURCHASE_API_GUIDE.md for detailed API documentation
echo 📮 Import Purchase_API_Postman_Collection.json into Postman for GUI testing

pause
