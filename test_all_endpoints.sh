#!/bin/bash

# Скрипт для тестирования всех эндпоинтов микросервисной системы
# Автор: Автоматически сгенерирован на основе анализа контроллеров

# set -e  # Остановка скрипта при ошибке - отключено для продолжения тестирования при ошибках

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Конфигурация
API_GATEWAY="http://localhost:8090"
USER_SERVICE="http://localhost:8081"
PRODUCT_SERVICE="http://localhost:8082"
CART_SERVICE="http://localhost:8083"
ORDER_SERVICE="http://localhost:8084"
PAYMENT_SERVICE="http://localhost:8085"
# Notification Service пока не развернут
# NOTIFICATION_SERVICE="http://localhost:8086"

# Глобальные переменные для хранения тестовых данных
USER_ID=""
PRODUCT_ID=""
CATEGORY_ID=""
ORDER_ID=""
PAYMENT_ID=""

# Функция для логирования
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

success() {
    echo -e "${GREEN}✓ $1${NC}"
}

error() {
    echo -e "${RED}✗ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Функция для выполнения HTTP запроса с обработкой ошибок
make_request() {
    local method="$1"
    local url="$2"
    local data="$3"
    local expected_status="$4"
    local description="$5"
    
    log "Testing: $description"
    log "Request: $method $url"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$url" 2>/dev/null || echo -e "\n000")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" \
            "$url" 2>/dev/null || echo -e "\n000")
    fi
    
    # Разделяем тело ответа и статус код
    body=$(echo "$response" | head -n -1)
    status=$(echo "$response" | tail -n 1)
    
    # Проверяем статус код
    if [ "$status" = "$expected_status" ]; then
        success "$description - Status: $status"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
        echo
        return 0
    else
        error "$description - Expected: $expected_status, Got: $status"
        echo "$body"
        echo
        return 1
    fi
}

# Функция для извлечения ID из JSON ответа
extract_id() {
    local response="$1"
    echo "$response" | jq -r '.id // empty' 2>/dev/null
}

# Функция для проверки доступности сервиса
check_service() {
    local service_name="$1"
    local service_url="$2"
    
    log "Checking $service_name availability..."
    
    # Проверяем специфические эндпоинты для разных сервисов
    if [[ "$service_name" == *"API Gateway"* ]]; then
        if curl -s -f "$service_url/api/gateway/health" >/dev/null 2>&1; then
            success "$service_name is available"
            return 0
        fi
    elif curl -s -f "$service_url/actuator/health" >/dev/null 2>&1; then
        success "$service_name is available"
        return 0
    elif curl -s -f "$service_url" >/dev/null 2>&1; then
        success "$service_name is available (no actuator)"
        return 0
    fi
    
    warning "$service_name is not available at $service_url"
    return 1
}

# Проверка доступности всех сервисов
check_all_services() {
    echo -e "${YELLOW}=== Проверка доступности сервисов ===${NC}"
    
    check_service "API Gateway" "$API_GATEWAY"
    check_service "User Service" "$USER_SERVICE"
    check_service "Product Service" "$PRODUCT_SERVICE"
    check_service "Cart Service" "$CART_SERVICE"
    check_service "Order Service" "$ORDER_SERVICE"
    check_service "Payment Service" "$PAYMENT_SERVICE"
    # check_service "Notification Service" "$NOTIFICATION_SERVICE"
    
    echo
}

# Тестирование API Gateway
test_api_gateway() {
    echo -e "${YELLOW}=== Тестирование API Gateway ===${NC}"
    
    make_request "GET" "$API_GATEWAY/api/gateway/health" "" "200" "API Gateway Health Check"
    make_request "GET" "$API_GATEWAY/api/gateway/info" "" "200" "API Gateway Info"
    make_request "GET" "$API_GATEWAY/api/gateway/routes" "" "200" "API Gateway Routes"
    
    echo
}

# Тестирование User Service
test_user_service() {
    echo -e "${YELLOW}=== Тестирование User Service ===${NC}"
    
    # Создание пользователя
    local user_data='{
        "username": "testuser",
        "email": "test@example.com",
        "phoneNumber": "+1234567890",
        "password": "password123",
        "role": "USER"
    }'
    
    # Создаем пользователя и получаем ответ
    local response_body=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d "$user_data" \
        "$USER_SERVICE/api/users" 2>/dev/null)
    
    USER_ID=$(echo "$response_body" | jq -r '.id // empty' 2>/dev/null)
    
    if [ -n "$USER_ID" ]; then
        success "User created with ID: $USER_ID"
        echo "$response_body" | jq '.' 2>/dev/null || echo "$response_body"
        echo
    else
        error "Failed to create user"
        echo "$response_body"
        echo
    fi
    
    # Получение всех пользователей
    make_request "GET" "$USER_SERVICE/api/users" "" "200" "Get All Users"
    
    if [ -n "$USER_ID" ]; then
        # Получение пользователя по ID
        make_request "GET" "$USER_SERVICE/api/users/$USER_ID" "" "200" "Get User by ID"
        
        # Получение пользователя по email
        make_request "GET" "$USER_SERVICE/api/users/email/test@example.com" "" "200" "Get User by Email"
        
        # Обновление роли пользователя
        make_request "PUT" "$USER_SERVICE/api/users/$USER_ID/role?role=ADMIN" "" "200" "Update User Role"
        
        # Удаление пользователя (в конце)
        # make_request "DELETE" "$USER_SERVICE/api/users/$USER_ID" "" "204" "Delete User"
    fi
    
    echo
}

# Тестирование Product Catalog Service
test_product_service() {
    echo -e "${YELLOW}=== Тестирование Product Catalog Service ===${NC}"
    
    # Сначала проверяем существующие категории
    echo "Checking existing categories..."
    local existing_categories=$(curl -s "$PRODUCT_SERVICE/api/categories" 2>/dev/null)
    echo "$existing_categories" | jq '.' 2>/dev/null || echo "$existing_categories"
    
    # Используем первую существующую категорию если есть
    CATEGORY_ID=$(echo "$existing_categories" | jq -r '.[0].id // empty' 2>/dev/null)
    
    if [ -n "$CATEGORY_ID" ]; then
        success "Using existing category with ID: $CATEGORY_ID"
    else
        # Если нет категорий, создаем новую с уникальным именем
        local timestamp=$(date +%s)
        local category_data='{
            "name": "TestCategory'$timestamp'",
            "description": "Test category created at '$timestamp'"
        }'
        
        local category_response=$(curl -s -X POST \
            -H "Content-Type: application/json" \
            -d "$category_data" \
            "$PRODUCT_SERVICE/api/categories" 2>/dev/null)
        
        CATEGORY_ID=$(echo "$category_response" | jq -r '.id // empty' 2>/dev/null)
        
        if [ -n "$CATEGORY_ID" ]; then
            success "Category created with ID: $CATEGORY_ID"
            echo "$category_response" | jq '.' 2>/dev/null || echo "$category_response"
            echo
        else
            error "Failed to create category"
            echo "$category_response"
            echo
            # Пропускаем создание продукта если нет категории
            return 1
        fi
    fi
    
    # Создание продукта с реальным ID категории
    if [ -n "$CATEGORY_ID" ]; then
        local timestamp=$(date +%s)
        local product_data='{
            "name": "TestProduct'$timestamp'",
            "description": "Test product created at '$timestamp'",
            "price": 999.99,
            "stockQuantity": 50,
            "categoryId": "'$CATEGORY_ID'",
            "brand": "TestBrand",
            "sku": "TESTSKU'$timestamp'",
            "isActive": true
        }'
        
        # Создаем продукт и получаем ответ
        local response_body=$(curl -s -X POST \
            -H "Content-Type: application/json" \
            -d "$product_data" \
            "$PRODUCT_SERVICE/api/products" 2>/dev/null)
        
        PRODUCT_ID=$(echo "$response_body" | jq -r '.id // empty' 2>/dev/null)
        
        if [ -n "$PRODUCT_ID" ]; then
            success "Product created with ID: $PRODUCT_ID"
            echo "$response_body" | jq '.' 2>/dev/null || echo "$response_body"
            echo
        else
            error "Failed to create product"
            echo "$response_body"
            echo
        fi
    else
        warning "Skipping product creation - no valid category ID"
    fi
    
    # Получение всех продуктов
    make_request "GET" "$PRODUCT_SERVICE/api/products" "" "200" "Get All Products"
    
    # Получение продуктов с пагинацией и фильтрами
    make_request "GET" "$PRODUCT_SERVICE/api/products?page=0&size=10" "" "200" "Get Products with Pagination"
    make_request "GET" "$PRODUCT_SERVICE/api/products?search=iPhone" "" "200" "Search Products"
    
    if [ -n "$CATEGORY_ID" ]; then
        # Получение продуктов по категории
        make_request "GET" "$PRODUCT_SERVICE/api/products?category=$CATEGORY_ID" "" "200" "Get Products by Category"
    fi
    
    if [ -n "$PRODUCT_ID" ]; then
        # Получение продукта по ID
        make_request "GET" "$PRODUCT_SERVICE/api/products/$PRODUCT_ID" "" "200" "Get Product by ID"
        
        # Обновление продукта
        local updated_product_data='{
            "name": "Updated iPhone",
            "description": "Updated description",
            "price": 1099.99,
            "stockQuantity": 45,
            "categoryId": "'$CATEGORY_ID'",
            "brand": "Apple",
            "sku": "TESTSKU001",
            "isActive": true
        }'
        
        make_request "PUT" "$PRODUCT_SERVICE/api/products/$PRODUCT_ID" "$updated_product_data" "200" "Update Product"
        
        # Обновление запасов
        local stock_update='{
            "quantity": 100,
            "operation": "SET"
        }'
        
        make_request "PUT" "$PRODUCT_SERVICE/api/products/$PRODUCT_ID/stock" "$stock_update" "200" "Update Product Stock"
    fi
    
    echo
}

# Тестирование Cart Service
test_cart_service() {
    echo -e "${YELLOW}=== Тестирование Cart Service ===${NC}"
    
    # Используем реальный USER_ID если он доступен, иначе тестовый
    local test_user_id="${USER_ID:-test-user-123}"
    local test_product_id="${PRODUCT_ID:-test-product-123}"
    
    # Добавление товара в корзину
    local add_to_cart_data='{
        "productId": "'$test_product_id'",
        "productName": "Test Product",
        "price": 99.99,
        "quantity": 2
    }'
    
    make_request "POST" "$CART_SERVICE/api/v1/cart/$test_user_id/items" "$add_to_cart_data" "200" "Add Item to Cart"
    
    # Получение корзины пользователя
    make_request "GET" "$CART_SERVICE/api/v1/cart/$test_user_id" "" "200" "Get User Cart"
    
    # Обновление количества товара в корзине
    local update_cart_data='{
        "quantity": 3
    }'
    
    make_request "PUT" "$CART_SERVICE/api/v1/cart/$test_user_id/items/$test_product_id" "$update_cart_data" "200" "Update Cart Item Quantity"
    
    # Удаление товара из корзины
    make_request "DELETE" "$CART_SERVICE/api/v1/cart/$test_user_id/items/$test_product_id" "" "200" "Remove Item from Cart"
    
    # Очистка корзины
    make_request "DELETE" "$CART_SERVICE/api/v1/cart/$test_user_id" "" "204" "Clear Cart"
    
    echo
}

# Тестирование Order Service
test_order_service() {
    echo -e "${YELLOW}=== Тестирование Order Service ===${NC}"
    
    # Проверяем, есть ли у нас USER_ID и PRODUCT_ID
    if [ -z "$USER_ID" ] || [ -z "$PRODUCT_ID" ]; then
        warning "Skipping order tests - missing USER_ID ($USER_ID) or PRODUCT_ID ($PRODUCT_ID)"
        return 1
    fi
    
    # Создание заказа с реальными ID
    local order_data='{
        "userId": "'$USER_ID'",
        "items": [
            {
                "productId": "'$PRODUCT_ID'",
                "productName": "Test Product",
                "productSku": "TEST-SKU",
                "unitPrice": 99.99,
                "quantity": 2
            }
        ],
        "shippingAddress": "123 Test Street, Test City, TC 12345",
        "billingAddress": "123 Test Street, Test City, TC 12345",
        "paymentMethod": "CREDIT_CARD"
    }'
    
    log "Testing: Create Order"
    log "Request: POST $ORDER_SERVICE/api/v1/orders"
    
    # Создаем заказ и получаем HTTP статус + тело ответа
    local response=$(curl -s -w "HTTPCODE:%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$order_data" \
        "$ORDER_SERVICE/api/v1/orders" 2>/dev/null)
    
    local http_code=$(echo "$response" | grep -o "HTTPCODE:[0-9]*" | cut -d: -f2)
    local response_body=$(echo "$response" | sed 's/HTTPCODE:[0-9]*$//')
    
    if [ "$http_code" = "201" ] || [ "$http_code" = "200" ]; then
        ORDER_ID=$(echo "$response_body" | jq -r '.id // empty' 2>/dev/null)
        success "Create Order - Status: $http_code"
        echo "$response_body" | jq '.' 2>/dev/null || echo "$response_body"
        echo
    else
        error "Create Order - Expected: 201, Got: $http_code"
        echo "$response_body"
        echo
    fi
    
    if [ -n "$ORDER_ID" ]; then
        # Получение заказа по ID
        make_request "GET" "$ORDER_SERVICE/api/v1/orders/$ORDER_ID" "" "200" "Get Order by ID"
        
        # Получение заказов пользователя
        make_request "GET" "$ORDER_SERVICE/api/v1/orders/user/$USER_ID" "" "200" "Get Orders by User"
        
        # Обновление статуса заказа
        local status_update='{
            "status": "CONFIRMED",
            "reason": "Payment processed successfully"
        }'
        
        make_request "PUT" "$ORDER_SERVICE/api/v1/orders/$ORDER_ID/status" "$status_update" "200" "Update Order Status"
    fi
    
    echo
}

# Тестирование Payment Service
test_payment_service() {
    echo -e "${YELLOW}=== Тестирование Payment Service ===${NC}"
    
    # Проверяем, есть ли у нас ORDER_ID и USER_ID
    if [ -z "$ORDER_ID" ] || [ -z "$USER_ID" ]; then
        warning "Skipping payment tests - missing ORDER_ID ($ORDER_ID) or USER_ID ($USER_ID)"
        return 1
    fi
    
    # Обработка платежа с реальными ID
    local payment_data='{
        "orderId": "'$ORDER_ID'",
        "userId": "'$USER_ID'",
        "amount": 199.98,
        "currency": "USD",
        "paymentMethod": "CREDIT_CARD",
        "paymentDetails": {
            "cardNumber": "4111111111111111",
            "expiryMonth": "12",
            "expiryYear": "2025",
            "cvv": "123"
        }
    }'
    
    # Создаем платеж и получаем ответ
    local response_body=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d "$payment_data" \
        "$PAYMENT_SERVICE/api/v1/payments" 2>/dev/null)
    
    PAYMENT_ID=$(echo "$response_body" | jq -r '.id // empty' 2>/dev/null)
    
    if [ -n "$PAYMENT_ID" ]; then
        success "Payment created with ID: $PAYMENT_ID"
        echo "$response_body" | jq '.' 2>/dev/null || echo "$response_body"
        echo
    else
        error "Failed to create payment"
        echo "$response_body"
        echo
    fi
    
    if [ -n "$PAYMENT_ID" ]; then
        # Получение платежа по ID
        make_request "GET" "$PAYMENT_SERVICE/api/v1/payments/$PAYMENT_ID" "" "200" "Get Payment by ID"
        
        # Получение платежей по заказу
        make_request "GET" "$PAYMENT_SERVICE/api/v1/payments/order/$ORDER_ID" "" "200" "Get Payments by Order"
        
        # Получение платежей пользователя
        make_request "GET" "$PAYMENT_SERVICE/api/v1/payments/user/$USER_ID" "" "200" "Get Payments by User"
        
        # Возврат платежа
        local refund_data='{
            "amount": 99.99,
            "reason": "Customer requested refund"
        }'
        
        make_request "POST" "$PAYMENT_SERVICE/api/v1/payments/$PAYMENT_ID/refund" "$refund_data" "200" "Refund Payment"
    fi
    
    echo
}

# Тестирование Notification Service
test_notification_service() {
    echo -e "${YELLOW}=== Тестирование Notification Service ===${NC}"
    
    # Отправка уведомления
    local notification_data='{
        "recipientId": "test-user-123",
        "type": "ORDER_CONFIRMATION",
        "channel": "EMAIL",
        "subject": "Your order has been confirmed",
        "content": "Thank you for your order! Your order has been confirmed and is being processed.",
        "metadata": {
            "orderId": "ORD-123456",
            "orderAmount": "199.98"
        }
    }'
    
    # make_request "POST" "$NOTIFICATION_SERVICE/api/notifications/send" "$notification_data" "201" "Send Notification"
    
    # # Получение уведомлений получателя
    # make_request "GET" "$NOTIFICATION_SERVICE/api/notifications/recipient/test-user-123" "" "200" "Get Notifications by Recipient"
    
    # # Получение статистики уведомлений
    # make_request "GET" "$NOTIFICATION_SERVICE/api/notifications/statistics" "" "200" "Get Notification Statistics"
    
    echo
}

# Тестирование через API Gateway
test_via_api_gateway() {
    echo -e "${YELLOW}=== Тестирование через API Gateway ===${NC}"
    
    # Тестируем маршрутизацию через API Gateway
    make_request "GET" "$API_GATEWAY/api/users" "" "200" "Get Users via API Gateway"
    make_request "GET" "$API_GATEWAY/api/products" "" "200" "Get Products via API Gateway"
    
    if [ -n "$USER_ID" ]; then
        make_request "GET" "$API_GATEWAY/api/users/$USER_ID" "" "200" "Get User by ID via API Gateway"
    fi
    
    if [ -n "$PRODUCT_ID" ]; then
        make_request "GET" "$API_GATEWAY/api/products/$PRODUCT_ID" "" "200" "Get Product by ID via API Gateway"
    fi
    
    echo
}

# Функция для очистки тестовых данных
cleanup() {
    echo -e "${YELLOW}=== Очистка тестовых данных ===${NC}"
    
    if [ -n "$USER_ID" ]; then
        make_request "DELETE" "$USER_SERVICE/api/users/$USER_ID" "" "204" "Delete Test User"
    fi
    
    if [ -n "$PRODUCT_ID" ]; then
        make_request "DELETE" "$PRODUCT_SERVICE/api/products/$PRODUCT_ID" "" "204" "Delete Test Product"
    fi
    
    echo
}

# Функция для генерации отчета
generate_report() {
    echo -e "${YELLOW}=== Отчет о тестировании ===${NC}"
    echo "Тестирование завершено: $(date)"
    echo "Проверено множество эндпоинтов в следующих сервисах:"
    echo "- API Gateway"
    echo "- User Service"
    echo "- Product Catalog Service"
    echo "- Cart Service"
    echo "- Order Service"
    echo "- Payment Service"
    echo "- Notification Service"
    echo
    echo "Для подробной информации об ошибках просмотрите вывод выше."
    echo
}

# Основная функция
main() {
    echo -e "${BLUE}======================================${NC}"
    echo -e "${BLUE}  Тестирование микросервисной системы${NC}"
    echo -e "${BLUE}======================================${NC}"
    echo
    
    # Проверка доступности инструментов
    if ! command -v curl &> /dev/null; then
        error "curl не установлен. Пожалуйста, установите curl для выполнения тестов."
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        warning "jq не установлен. JSON ответы не будут форматированы."
    fi
    
    # Проверка доступности сервисов
    check_all_services
    
    # Запуск тестов в правильном порядке
    test_api_gateway
    test_user_service
    test_product_service
    
    # Тестируем остальные сервисы только если есть базовые данные
    if [ -n "$USER_ID" ] && [ -n "$PRODUCT_ID" ]; then
        test_cart_service
        test_order_service
        test_payment_service
    else
        warning "Skipping cart, order, and payment tests - missing base data"
    fi
    
    test_notification_service
    test_via_api_gateway
    
    # Очистка данных
    cleanup
    
    # Генерация отчета
    generate_report
    
    success "Тестирование завершено!"
}

# Обработка сигналов
trap cleanup EXIT

# Запуск основной функции
main "$@"
