# Лабораторная работа #4: Микросервисная архитектура с асинхронной обработкой задач

## Описание проекта

Микросервисная система электронной коммерции с асинхронной обработкой задач, планировщиками и интеграцией с внешними системами.

## Архитектура системы

### Микросервисы

1. **API Gateway** (`порт 8090`)
   - Маршрутизация запросов
   - Spring Cloud Gateway
   - CORS и Security

2. **User Service** (`порт 8081`)
   - Управление пользователями
   - PostgreSQL база данных
   - RabbitMQ события

3. **Product Catalog Service** (`порт 8082`)
   - Каталог товаров
   - Управление запасами
   - PostgreSQL база данных
   - @Scheduled задачи проверки запасов

4. **Cart Service** (`порт 8083`)
   - Корзина покупок
   - Cassandra база данных
   - @Scheduled статистика корзин

5. **Order Service** (`порт 8084`)
   - Управление заказами
   - PostgreSQL база данных
   - @Async обработка событий

6. **Payment Service** (`порт 8085`)
   - Обработка платежей
   - JCA-подобные адаптеры (Stripe, PayPal)
   - PostgreSQL база данных
   - @Async и @Scheduled обработка

### Инфраструктурные компоненты

- **RabbitMQ** (`порты 5672/15672`) - Асинхронный обмен сообщениями
- **PostgreSQL** (порты 5433-5436) - Основные базы данных
- **Cassandra** (`порт 9042`) - База для корзин

## Ключевые требования и их реализация

### ✅ RabbitMQ для асинхронного обмена сообщениями
- Topic Exchange архитектура
- События между всеми сервисами
- @RabbitListener обработчики

### ✅ Планировщик задач Spring (@Scheduled)
- **Product Service**: Проверка низких запасов (ежедневно), обновление цен (каждые 6 часов)
- **Cart Service**: Статистика корзин (каждые 30 минут)
- **Payment Service**: Обработка зависших платежей (каждые 5 минут), статистика (каждый час)

### ✅ Асинхронная обработка (@Async)
- **Order Service**: Асинхронная публикация событий
- **Payment Service**: Асинхронная обработка платежей
- **Cart Service**: Асинхронная обработка событий заказов

### ✅ JCA-подобная интеграция
- **Payment Service**: Абстрактные адаптеры для Stripe и PayPal
- Унифицированный интерфейс PaymentGatewayService
- Конфигурируемые параметры подключения
- ✅ Camunda BPM для динамического управления процессами
- ✅ Формы Camunda для пользовательских интерфейсов

## Технологии

- **Spring Boot 3.2** - Основной фреймворк
- **Spring Cloud Gateway** - API Gateway
- **Spring Data JPA/Cassandra** - Работа с БД
- **Spring AMQP** - RabbitMQ интеграция
- **Spring Security** - Безопасность
- **Docker Compose** - Контейнеризация
- **Kotlin** - Язык программирования
- **Camunda BPM** - Управление бизнес-процессами в embedded режиме

## Развертывание

### Быстрый старт

```bash
# Сборка сервисов
./scripts/gradle-build.sh

# Запуск инфраструктуры
docker-compose up -d rabbitmq postgres-user postgres-product postgres-order postgres-payment cassandra

# Ожидание готовности (2-3 минуты)

# Запуск сервисов
docker-compose up -d user-service product-catalog-service cart-service order-service payment-service api-gateway
```

## Полезные URL

### Сервисы
- API Gateway: http://localhost:8090
- User Service: http://localhost:8081
- Product Service: http://localhost:8082
- Cart Service: http://localhost:8083
- Order Service: http://localhost:8084
- Payment Service: http://localhost:8085

### Управление
- RabbitMQ Management: http://localhost:15672 (admin/admin123)

## API Примеры

**Создание пользователя:**
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com"}'
```

**Добавление в корзину:**
```bash
curl -X POST http://localhost:8083/api/v1/cart/user1/items \
  -H "Content-Type: application/json" \
  -d '{"productId":"1","productName":"Test","price":99.99,"quantity":2}'
```

**Создание заказа:**
```bash
curl -X POST http://localhost:8084/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"1","items":[{"productId":"1","quantity":2}]}'
```

## Особенности реализации

### Асинхронная обработка
- События интеграции через RabbitMQ
- @Async методы для неблокирующих операций
- CompletableFuture для асинхронных результатов

### Планировщики задач
- Проверка запасов и генерация статистики
- Обработка зависших операций
- Очистка устаревших данных

### JCA-подобная архитектура
- Абстрактные интерфейсы для внешних систем
- Конкретные реализации адаптеров
- Конфигурируемые параметры

### Отказоустойчивость
- Health checks для всех компонентов
- Retry механизмы в RabbitMQ
- Graceful degradation

## Структура проекта

```
microservices/
├── api-gateway/           # Spring Cloud Gateway
├── user-service/          # Управление пользователями
├── product-catalog-service/ # Каталог товаров
├── cart-service/          # Корзина (Cassandra)
├── order-service/         # Заказы
└── payment-service/       # Платежи (JCA адаптеры)
```

## Мониторинг

```bash
# Статус контейнеров
docker-compose ps

# Логи сервиса
docker-compose logs user-service

# Метрики
curl http://localhost:8081/actuator/health
```

## Заключение

Система реализует:
- ✅ Микросервисную архитектуру
- ✅ Асинхронную обработку через RabbitMQ
- ✅ Планировщики задач (@Scheduled)
- ✅ JCA-подобную интеграцию
- ✅ Event-driven архитектуру
- ✅ Контейнеризацию и оркестрацию

### Troubleshooting Camunda Tasklist

Если задачи не отображаются в интерфейсе Camunda, убедитесь, что в `application.yml`
выключена генерация уникального имени процессного движка:

```yaml
camunda:
  bpm:
    generate-unique-process-engine-name: false
```

После изменения перезапустите соответствующий сервис.

Если вместо формы появляется сообщение `Form failure: The context path is either empty or not defined`,
укажите путь веб-приложения:

```yaml
camunda:
  bpm:
    webapp:
      application-path: /
```

После этого вновь перезапустите сервис.
