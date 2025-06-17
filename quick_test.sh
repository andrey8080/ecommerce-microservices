#!/bin/bash

# Быстрая диагностика сервисов
echo "=== БЫСТРАЯ ДИАГНОСТИКА СЕРВИСОВ ==="

echo "1. API Gateway:"
timeout 5 curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8090/api/gateway/health

echo "2. User Service:"
timeout 5 curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8081/api/users

echo "3. Product Service:"
timeout 5 curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8082/api/products

echo "4. Cart Service:"
timeout 5 curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8083/api/v1/cart/test-user

echo "5. Order Service:"
timeout 5 curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8084/api/v1/orders/550e8400-e29b-41d4-a716-446655440001

echo "6. Payment Service:"
timeout 5 curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8085/api/v1/payments/550e8400-e29b-41d4-a716-446655440001

echo ""
echo "=== ТЕСТИРОВАНИЕ ОСНОВНЫХ ОПЕРАЦИЙ ==="

echo "Тест 1: Создание пользователя"
USER_RESPONSE=$(timeout 10 curl -s -w "HTTPCODE:%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "username": "quicktest_user",
    "email": "quicktest@example.com",
    "phoneNumber": "+1111111111",
    "password": "testpass123"
  }' \
  http://localhost:8081/api/users)
echo "User creation: $USER_RESPONSE"

echo ""
echo "Тест 2: Добавление в корзину"
CART_RESPONSE=$(timeout 10 curl -s -w "HTTPCODE:%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "quick-test-product",
    "productName": "Quick Test Product", 
    "price": 50.00,
    "quantity": 1
  }' \
  http://localhost:8083/api/v1/cart/quicktest-user/items)
echo "Cart addition: $CART_RESPONSE"

echo ""
echo "Тест 3: Создание заказа"
ORDER_RESPONSE=$(timeout 10 curl -s -w "HTTPCODE:%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {
        "productId": "quick-test-product",
        "productName": "Quick Test Product",
        "productSku": "QTP-001", 
        "unitPrice": 50.00,
        "quantity": 1
      }
    ],
    "shippingAddress": "Quick Test Address",
    "billingAddress": "Quick Test Address",
    "paymentMethod": "CREDIT_CARD"
  }' \
  http://localhost:8084/api/v1/orders)
echo "Order creation: $ORDER_RESPONSE"

echo ""
echo "=== ДИАГНОСТИКА ЗАВЕРШЕНА ==="
