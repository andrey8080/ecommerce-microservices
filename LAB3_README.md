# Лабораторная работа #3: Микросервисная архитектура с асинхронной обработкой задач

## Описание проекта

Данная лабораторная работа реализует микросервисную архитектуру интернет-магазина с асинхронной обработкой задач, распределением бизнес-логики между вычислительными узлами, планировщиком задач и интеграцией с внешними системами.

## Архитектура системы

### Микросервисы

1. **API Gateway** (порт 8080)
   - Единая точка входа для всех запросов
   - Маршрутизация запросов к соответствующим микросервисам
   - Rate limiting и circuit breaker
   - Аутентификация и авторизация

2. **User Service** (порт 8081)
   - Управление пользователями
   - Регистрация и аутентификация
   - Профили пользователей

3. **Product Catalog Service** (порт 8082)
   - Каталог товаров
   - Управление категориями
   - Поиск и фильтрация товаров

4. **Cart Service** (порт 8083)
   - Корзина покупок
   - Добавление/удаление товаров
   - Расчет общей стоимости
   - Обнаружение заброшенных корзин

5. **Order Service** (порт 8084)
   - Создание и управление заказами
   - Распределенные транзакции
   - Интеграция с сервисами запасов и платежей

6. **Payment Service** (порт 8085)
   - Обработка платежей
   - Интеграция с внешними платежными шлюзами
   - JCA-подобная архитектура для провайдеров

7. **Notification Service** (порт 8086)
   - Отправка уведомлений (Email, SMS)
   - Шаблоны уведомлений
   - Асинхронная обработка событий

### Инфраструктурные компоненты

- **RabbitMQ** (порты 5672, 15672) - асинхронный обмен сообщениями
- **Consul** (порт 8500) - service discovery и конфигурация
- **Redis** (порт 6379) - кэширование и rate limiting
- **PostgreSQL** - основная база данных для большинства сервисов
- **Cassandra** - база данных для cart-service
- **MailHog** (порты 1025, 8025) - тестирование email уведомлений

## Ключевые особенности реализации

### 1. Асинхронная обработка с RabbitMQ

Система использует RabbitMQ для асинхронного обмена сообщениями между микросервисами:

- **Topic Exchange** для гибкой маршрутизации сообщений
- **Durable Queues** для надежности доставки
- **Dead Letter Queues** для обработки ошибок
- **Event-driven архитектура** для слабой связанности сервисов

### 2. Планировщики задач (@Scheduled)

Каждый сервис содержит планировщики для фоновых операций:

- **Cart Service**: Очистка заброшенных корзин каждый час, генерация статистики каждые 30 минут
- **Order Service**: Обработка просроченных заказов каждые 5 минут, генерация статистики каждый час
- **Payment Service**: Обработка зависших платежей каждые 5 минут, генерация статистики каждый час
- **Notification Service**: Повторная отправка неудачных уведомлений каждые 5 минут, очистка старых записей ежедневно

### 3. Распределенные транзакции

Order Service реализует распределенные транзакции с помощью паттерна Saga:

- **Choreography-based Saga** для координации между сервисами
- **Компенсирующие транзакции** для отката в случае ошибок
- **Eventual consistency** между микросервисами

### 4. JCA-подобная интеграция

Payment Service использует JCA-подобную архитектуру для интеграции с внешними системами:

- **PaymentGatewayService** интерфейс для унификации провайдеров
- **StripePaymentGateway** и **PayPalPaymentGateway** для различных платежных систем
- **Connection pooling** и **failover** механизмы

### 5. Асинхронная обработка (@Async)

Все сервисы используют асинхронную обработку для неблокирующих операций:

- **Thread pools** для различных типов задач
- **CompletableFuture** для асинхронных операций
- **Error handling** и **retry mechanisms**

## Развертывание и запуск

### Предварительные требования

- Docker 20.10+
- Docker Compose v2.0+
- 8GB+ RAM
- 20GB+ свободного места на диске

### Быстрый старт

1. **Клонирование и переход в директорию:**
   ```bash
   cd "/home/andrey/Загрузки/Telegram Desktop/software_systems_business_logic_lab3"
   ```

2. **Запуск всей системы:**
   ```bash
   ./scripts/deploy.sh start
   ```

3. **Проверка статуса:**
   ```bash
   ./scripts/deploy.sh status
   ```

4. **Просмотр логов:**
   ```bash
   ./scripts/deploy.sh logs
   ```

### Управление системой

#### Основные команды

```bash
# Запуск системы
./scripts/deploy.sh start

# Остановка системы
./scripts/deploy.sh stop

# Перезапуск системы
./scripts/deploy.sh restart

# Проверка статуса
./scripts/deploy.sh status

# Проверка здоровья сервисов
./scripts/deploy.sh health

# Просмотр логов всех сервисов
./scripts/deploy.sh logs

# Просмотр логов конкретного сервиса
./scripts/deploy.sh logs api-gateway

# Масштабирование сервиса
./scripts/deploy.sh scale user-service 3

# Очистка системы
./scripts/deploy.sh cleanup

# Показать полезные URL
./scripts/deploy.sh urls
```

#### Интеграционное тестирование

```bash
# Запуск полного набора интеграционных тестов
./test-microservices.sh

# Запуск тестов конкретного сервиса
./scripts/integration-test.sh user-service

# Мониторинг системы
./scripts/monitor.sh
```

## API Документация

### Основные эндпоинты

#### API Gateway (http://localhost:8080)
- `GET /api/health` - Проверка здоровья
- `GET /api/users/**` - Проксирование к User Service
- `GET /api/products/**` - Проксирование к Product Service
- `GET /api/cart/**` - Проксирование к Cart Service
- `GET /api/orders/**` - Проксирование к Order Service
- `GET /api/payments/**` - Проксирование к Payment Service
- `GET /api/notifications/**` - Проксирование к Notification Service

#### User Service (http://localhost:8081)
- `POST /api/users` - Создание пользователя
- `GET /api/users/{id}` - Получение пользователя
- `PUT /api/users/{id}` - Обновление пользователя
- `POST /api/users/authenticate` - Аутентификация

#### Product Service (http://localhost:8082)
- `GET /api/products` - Список товаров
- `GET /api/products/{id}` - Получение товара
- `POST /api/products` - Создание товара (Admin)
- `PUT /api/products/{id}` - Обновление товара (Admin)

#### Cart Service (http://localhost:8083)
- `GET /api/cart/{userId}` - Получение корзины
- `POST /api/cart/add` - Добавление в корзину
- `PUT /api/cart/update` - Обновление количества
- `DELETE /api/cart/{userId}/items/{productId}` - Удаление из корзины

#### Order Service (http://localhost:8084)
- `POST /api/orders` - Создание заказа
- `GET /api/orders/{id}` - Получение заказа
- `GET /api/orders/user/{userId}` - Заказы пользователя
- `PUT /api/orders/{id}/status` - Обновление статуса

#### Payment Service (http://localhost:8085)
- `POST /api/payments/process` - Обработка платежа
- `GET /api/payments/{id}` - Получение платежа
- `GET /api/payments/order/{orderId}` - Платежи по заказу
- `GET /api/payments/statistics` - Статистика платежей

#### Notification Service (http://localhost:8086)
- `POST /api/notifications/send` - Отправка уведомления
- `GET /api/notifications/{id}` - Получение уведомления
- `GET /api/notifications/recipient/{recipientId}` - Уведомления получателя
- `GET /api/notifications/statistics` - Статистика уведомлений

### Swagger UI

Документация API доступна по адресам:
- User Service: http://localhost:8081/swagger-ui.html
- Product Service: http://localhost:8082/swagger-ui.html
- Cart Service: http://localhost:8083/swagger-ui.html
- Order Service: http://localhost:8084/swagger-ui.html
- Payment Service: http://localhost:8085/swagger-ui.html
- Notification Service: http://localhost:8086/swagger-ui.html

## Мониторинг и управление

### Web интерфейсы

- **Consul UI**: http://localhost:8500
- **RabbitMQ Management**: http://localhost:15672 (admin/admin123)
- **MailHog UI**: http://localhost:8025

### Health Checks

Все сервисы предоставляют health check эндпоинты:
- `GET /{service}/actuator/health`

### Метрики и мониторинг

Каждый сервис экспортирует метрики через Spring Boot Actuator:
- `GET /{service}/actuator/metrics`
- `GET /{service}/actuator/prometheus`

## Схема базы данных

### PostgreSQL (User, Product, Order, Payment Services)

Каждый сервис использует отдельную базу данных PostgreSQL с миграциями Flyway.

### Cassandra (Cart Service)

```cql
CREATE KEYSPACE cart_service WITH replication = {
    'class': 'SimpleStrategy',
    'replication_factor': 1
};

CREATE TABLE cart_service.carts (
    user_id text PRIMARY KEY,
    created_at timestamp,
    updated_at timestamp
);

CREATE TABLE cart_service.cart_items (
    user_id text,
    product_id text,
    quantity int,
    price decimal,
    added_at timestamp,
    PRIMARY KEY (user_id, product_id)
);
```

### MongoDB (Notification Service)

```javascript
// notifications collection
{
  _id: ObjectId,
  recipientId: String,
  recipientEmail: String,
  recipientPhone: String,
  type: String, // ORDER_CONFIRMATION, PAYMENT_SUCCESS, etc.
  channel: String, // EMAIL, SMS, PUSH
  subject: String,
  content: String,
  status: String, // PENDING, SENT, DELIVERED, FAILED
  priority: String, // LOW, NORMAL, HIGH, URGENT
  scheduledAt: Date,
  sentAt: Date,
  deliveredAt: Date,
  retryCount: Number,
  createdAt: Date,
  updatedAt: Date
}
```

## RabbitMQ Топология

### Exchanges

- `user.events` - События пользователей
- `product.events` - События товаров
- `cart.events` - События корзины
- `order.events` - События заказов
- `payment.events` - События платежей
- `notification.events` - События уведомлений
- `stock.events` - События складских запасов

### Queues и Routing Keys

#### Order Flow
- `order.created` ← `order.events` (order.created)
- `order.stock.reserve` ← `stock.events` (stock.reserve)
- `order.payment.process` ← `payment.events` (payment.process)

#### Notification Flow
- `notification.order.created` ← `order.events` (order.created)
- `notification.payment.processed` ← `payment.events` (payment.processed)
- `notification.cart.abandoned` ← `cart.events` (cart.abandoned)

## Тестирование

### Интеграционные тесты

Система включает полный набор интеграционных тестов:

```bash
# Запуск всех тестов
./test-microservices.sh

# Тест создания пользователя
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password","firstName":"Test"}'

# Тест создания заказа
curl -X POST http://localhost:8084/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"1","items":[{"productId":"1","quantity":2}]}'
```

### Нагрузочное тестирование

Для проведения нагрузочных тестов можно использовать включенные скрипты:

```bash
# Мониторинг производительности
./scripts/monitor.sh

# Масштабирование под нагрузкой
./scripts/deploy.sh scale user-service 3
./scripts/deploy.sh scale order-service 2
```

## Troubleshooting

### Общие проблемы

1. **Сервисы не запускаются**
   ```bash
   # Проверка логов
   ./scripts/deploy.sh logs
   
   # Проверка ресурсов
   docker stats
   ```

2. **Проблемы с базами данных**
   ```bash
   # Перезапуск баз данных
   docker compose -f docker-compose.yml restart postgres-user postgres-product
   ```

3. **Проблемы с RabbitMQ**
   ```bash
   # Проверка состояния очередей
   curl -u admin:admin123 http://localhost:15672/api/queues
   ```

### Логи и отладка

```bash
# Детальные логи конкретного сервиса
./scripts/deploy.sh logs order-service
```

## Производительность и масштабирование

### Рекомендации по масштабированию

1. **Горизонтальное масштабирование**:
   ```bash
   ./scripts/deploy.sh scale user-service 3
   ./scripts/deploy.sh scale order-service 2
   ```

2. **Вертикальное масштабирование**:
   - Увеличение ресурсов в docker-compose.yml
   - Настройка JVM параметров

3. **Кэширование**:
   - Redis для кэширования часто запрашиваемых данных
   - Кэширование на уровне application

### Мониторинг производительности

```bash
# Использование ресурсов
docker stats

# Метрики приложений
curl http://localhost:8081/actuator/metrics

# Состояние очередей RabbitMQ
curl -u admin:admin123 http://localhost:15672/api/overview
```

## Безопасность

### Реализованные меры безопасности

1. **Аутентификация и авторизация** через API Gateway
2. **Rate limiting** для предотвращения злоупотреблений
3. **Circuit breaker** для обработки отказов
4. **Валидация входных данных** на всех уровнях
5. **Изоляция сервисов** через Docker containers

### Рекомендации для production

1. **HTTPS** для всех внешних соединений
2. **Service mesh** (Istio/Linkerd) для security policies
3. **Secrets management** (Vault/K8s secrets)
4. **Regular security scans** образов Docker

## Заключение

Данная лабораторная работа демонстрирует полную реализацию микросервисной архитектуры с:

- ✅ **Асинхронной обработкой задач** через RabbitMQ
- ✅ **Распределенными транзакциями** с паттерном Saga
- ✅ **Планировщиками задач** (@Scheduled) для фоновых операций
- ✅ **JCA-подобной интеграцией** с внешними системами
- ✅ **Event-driven архитектурой** для слабой связанности
- ✅ **Мониторингом и observability**
- ✅ **Горизонтальным масштабированием**
- ✅ **Fault tolerance** и circuit breakers

Система готова для production deployment с соответствующими настройками безопасности и мониторинга.

---

**Автор**: Лабораторная работа #3 по Системам программного обеспечения  
**Дата**: Июнь 2025  
**Версия**: 1.0
