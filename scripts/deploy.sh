#!/bin/bash

# Deployment and Management Script for Microservices Architecture
# Скрипт развертывания и управления микросервисной архитектурой

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Конфигурация
PROJECT_ROOT="/home/andrey/Документы/andrey8080_git/software_systems_business_logic_lab4"
COMPOSE_FILE="$PROJECT_ROOT/docker-compose.yml"

# Сервисы по порядку запуска
INFRASTRUCTURE_SERVICES=(
    "rabbitmq"
    "postgres-user"
    "postgres-product"
    "postgres-order"
    "postgres-payment"
    "cassandra"
)

MICROSERVICES=(
    "user-service"
    "product-catalog-service"
    "cart-service"
    "order-service"
    "payment-service"
    "api-gateway"
)

# Функция для отображения заголовка
print_header() {
    local title=$1
    echo -e "${BLUE}=================================${NC}"
    echo -e "${CYAN}$title${NC}"
    echo -e "${BLUE}=================================${NC}"
}

# Функция для логирования
log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    case $level in
        "INFO")
            echo -e "${GREEN}[$timestamp] INFO: $message${NC}"
            ;;
        "WARN")
            echo -e "${YELLOW}[$timestamp] WARN: $message${NC}"
            ;;
        "ERROR")
            echo -e "${RED}[$timestamp] ERROR: $message${NC}"
            ;;
        "DEBUG")
            echo -e "${PURPLE}[$timestamp] DEBUG: $message${NC}"
            ;;
    esac
}

# Функция для проверки зависимостей
check_dependencies() {
    log "INFO" "Проверка зависимостей..."
    
    local missing_deps=()
    
    if ! command -v docker &> /dev/null; then
        missing_deps+=("docker")
    fi
    
    # Проверяем docker compose как подкоманду
    if ! docker compose version &> /dev/null; then
        missing_deps+=("docker-compose")
    fi
    
    if ! command -v curl &> /dev/null; then
        missing_deps+=("curl")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        log "ERROR" "Отсутствуют зависимости: ${missing_deps[*]}"
        exit 1
    fi
    
    log "INFO" "Все зависимости установлены"
}

# Функция для создания сети Docker
create_docker_network() {
    local network_name="microservices-network"
    
    if ! docker network ls | grep -q "$network_name"; then
        log "INFO" "Создание Docker сети: $network_name"
        docker network create "$network_name" || log "WARN" "Сеть уже существует"
    else
        log "INFO" "Docker сеть уже существует: $network_name"
    fi
}

# Функция для сборки всех сервисов
build_services() {
    log "INFO" "Сборка всех микросервисов..."
    
    cd "$PROJECT_ROOT"
    
    # Сборка всех сервисов параллельно
    docker compose -f "$COMPOSE_FILE" build --parallel
    
    log "INFO" "Сборка завершена"
}

# Функция для запуска инфраструктуры
start_infrastructure() {
    log "INFO" "Запуск инфраструктурных сервисов..."
    
    cd "$PROJECT_ROOT"
    
    for service in "${INFRASTRUCTURE_SERVICES[@]}"; do
        log "INFO" "Запуск $service..."
        docker compose -f "$COMPOSE_FILE" up -d "$service"
        
        # Ждем запуска критических сервисов
        case $service in
            "consul"|"rabbitmq"|"redis")
                log "INFO" "Ожидание готовности $service..."
                sleep 15
                ;;
            "postgres-"*|"mongodb"|"cassandra")
                log "INFO" "Ожидание готовности $service..."
                sleep 20
                ;;
        esac
    done
    
    log "INFO" "Инфраструктура запущена"
}

# Функция для запуска микросервисов
start_microservices() {
    log "INFO" "Запуск микросервисов..."
    
    cd "$PROJECT_ROOT"
    
    for service in "${MICROSERVICES[@]}"; do
        log "INFO" "Запуск $service..."
        docker compose -f "$COMPOSE_FILE" up -d "$service"
        
        # Небольшая задержка между запусками
        sleep 10
    done
    
    log "INFO" "Микросервисы запущены"
}

# Функция для полного запуска системы
start_all() {
    print_header "ЗАПУСК МИКРОСЕРВИСНОЙ СИСТЕМЫ"
    
    check_dependencies
    create_docker_network
    build_services
    start_infrastructure
    
    log "INFO" "Ожидание готовности инфраструктуры..."
    sleep 30
    
    start_microservices
    
    log "INFO" "Ожидание готовности микросервисов..."
    sleep 60
    
    # Проверка статуса
    check_system_health
    
    print_urls
}

# Функция для остановки системы
stop_all() {
    print_header "ОСТАНОВКА МИКРОСЕРВИСНОЙ СИСТЕМЫ"
    
    cd "$PROJECT_ROOT"
    
    log "INFO" "Остановка всех сервисов..."
    docker compose -f "$COMPOSE_FILE" down
    
    log "INFO" "Система остановлена"
}

# Функция для перезапуска системы
restart_all() {
    print_header "ПЕРЕЗАПУСК МИКРОСЕРВИСНОЙ СИСТЕМЫ"
    
    stop_all
    sleep 10
    start_all
}

# Функция для отображения логов
show_logs() {
    local service=$1
    
    cd "$PROJECT_ROOT"
    
    if [ -z "$service" ]; then
        log "INFO" "Показ логов всех сервисов..."
        docker compose -f "$COMPOSE_FILE" logs -f --tail=100
    else
        log "INFO" "Показ логов сервиса: $service"
        docker compose -f "$COMPOSE_FILE" logs -f --tail=100 "$service"
    fi
}

# Функция для масштабирования сервиса
scale_service() {
    local service=$1
    local replicas=$2
    
    if [ -z "$service" ] || [ -z "$replicas" ]; then
        log "ERROR" "Использование: scale <service> <replicas>"
        return 1
    fi
    
    cd "$PROJECT_ROOT"
    
    log "INFO" "Масштабирование $service до $replicas реплик..."
    docker compose -f "$COMPOSE_FILE" up -d --scale "$service=$replicas" "$service"
    
    log "INFO" "Масштабирование завершено"
}

# Функция для очистки системы
cleanup() {
    print_header "ОЧИСТКА СИСТЕМЫ"
    
    cd "$PROJECT_ROOT"
    
    log "INFO" "Остановка и удаление контейнеров..."
    docker compose -f "$COMPOSE_FILE" down -v --remove-orphans
    
    log "INFO" "Удаление неиспользуемых образов..."
    docker image prune -f
    
    log "INFO" "Удаление неиспользуемых томов..."
    docker volume prune -f
    
    log "INFO" "Очистка завершена"
}

# Функция для проверки здоровья системы
check_system_health() {
    echo -e "${YELLOW}Проверка здоровья сервисов:${NC}"
    
    # Проверка API Gateway
    if curl -s http://localhost:8090/actuator/health > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} API Gateway (port 8090)"
    else
        echo -e "  ${RED}✗${NC} API Gateway (port 8090)"
    fi
    
    # Проверка User Service
    if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} User Service (port 8081)"
    else
        echo -e "  ${RED}✗${NC} User Service (port 8081)"
    fi
    
    # Проверка Product Catalog Service
    if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} Product Catalog Service (port 8082)"
    else
        echo -e "  ${RED}✗${NC} Product Catalog Service (port 8082)"
    fi
    
    # Проверка Order Service
    if curl -s http://localhost:8084/actuator/health > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} Order Service (port 8084)"
    else
        echo -e "  ${RED}✗${NC} Order Service (port 8084)"
    fi
    
    # Проверка Payment Service
    if curl -s http://localhost:8085/actuator/health > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} Payment Service (port 8085)"
    else
        echo -e "  ${RED}✗${NC} Payment Service (port 8085)"
    fi
    
    echo -e "${YELLOW}Camunda Web UI:${NC}"
    
    # Проверка Camunda UI на каждом сервисе
    if curl -s http://localhost:8081/camunda/ > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} User Service Camunda UI: http://localhost:8081/camunda/"
    else
        echo -e "  ${RED}✗${NC} User Service Camunda UI недоступен"
    fi
    
    if curl -s http://localhost:8082/camunda/ > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} Product Catalog Camunda UI: http://localhost:8082/camunda/"
    else
        echo -e "  ${RED}✗${NC} Product Catalog Camunda UI недоступен"
    fi
    
    if curl -s http://localhost:8084/camunda/ > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} Order Service Camunda UI: http://localhost:8084/camunda/"
    else
        echo -e "  ${RED}✗${NC} Order Service Camunda UI недоступен"
    fi
    
    if curl -s http://localhost:8085/camunda/ > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} Payment Service Camunda UI: http://localhost:8085/camunda/"
    else
        echo -e "  ${RED}✗${NC} Payment Service Camunda UI недоступен"
    fi
}

# Функция для отображения статуса
show_status() {
    print_header "СТАТУС СИСТЕМЫ"
    
    cd "$PROJECT_ROOT"
    
    echo -e "${YELLOW}Docker контейнеры:${NC}"
    docker compose -f "$COMPOSE_FILE" ps
    
    echo
    echo -e "${YELLOW}Использование ресурсов:${NC}"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"
    
    echo
    check_system_health
}

# Функция для отображения полезных URL
print_urls() {
    print_header "ПОЛЕЗНЫЕ URL"
    
    echo -e "${GREEN}Микросервисы:${NC}"
    echo "  API Gateway:         http://localhost:8090"
    echo "  User Service:        http://localhost:8081"
    echo "  Product Service:     http://localhost:8082"
    echo "  Order Service:       http://localhost:8084"
    echo "  Payment Service:     http://localhost:8085"
    
    echo
    echo -e "${GREEN}Camunda BPM UI:${NC}"
    echo "  User Service:        http://localhost:8081/camunda/"
    echo "  Product Service:     http://localhost:8082/camunda/"
    echo "  Order Service:       http://localhost:8084/camunda/"
    echo "  Payment Service:     http://localhost:8085/camunda/"
    echo "  (Логин: demo, Пароль: demo)"
    
    echo
    echo -e "${GREEN}Инфраструктура:${NC}"
    echo "  RabbitMQ UI:         http://localhost:15672 (admin/admin123)"
    
    echo
    echo -e "${GREEN}Базы данных:${NC}"
    echo "  PostgreSQL User:      localhost:5433"
    echo "  PostgreSQL Product:   localhost:5434"
    echo "  PostgreSQL Order:     localhost:5435"
    echo "  PostgreSQL Payment:   localhost:5436"
    echo "  Cassandra:           localhost:9042"
    
    echo
    echo -e "${GREEN}API Documentation:${NC}"
    echo "  User Service Swagger:     http://localhost:8081/swagger-ui.html"
    echo "  Product Service Swagger:  http://localhost:8082/swagger-ui.html"
    echo "  Order Service Swagger:    http://localhost:8084/swagger-ui.html"
    echo "  Payment Service Swagger:  http://localhost:8085/swagger-ui.html"
}

# Функция для выполнения миграций
run_migrations() {
    log "INFO" "Выполнение миграций баз данных..."
    
    # Для каждого PostgreSQL сервиса выполняем миграции
    local postgres_services=("user" "product" "order" "payment")
    
    for service in "${postgres_services[@]}"; do
        log "INFO" "Миграция для $service-service..."
        # Здесь можно добавить команды для выполнения миграций
    done
    
    log "INFO" "Миграции завершены"
}

# Функция для пересборки и перезапуска только микросервисов
rebuild_microservices() {
    print_header "ПЕРЕСБОРКА И ЗАПУСК МИКРОСЕРВИСОВ (без инфры)"

    cd "$PROJECT_ROOT"

    for service in "${MICROSERVICES[@]}"; do
        log "INFO" "Пересборка $service..."
        docker compose -f "$COMPOSE_FILE" build "$service"
    done

    for service in "${MICROSERVICES[@]}"; do
        log "INFO" "Перезапуск $service..."
        docker compose -f "$COMPOSE_FILE" up -d "$service"
        sleep 5
    done

    log "INFO" "Микросервисы пересобраны и запущены"
}


# Основная функция
main() {
    case "${1:-help}" in
        "start"|"up")
            start_all
            ;;
        "stop"|"down")
            stop_all
            ;;
        "restart")
            restart_all
            ;;
        "build")
            build_services
            ;;
        "status"|"ps")
            show_status
            ;;
        "logs")
            show_logs "$2"
            ;;
        "scale")
            scale_service "$2" "$3"
            ;;
        "cleanup"|"clean")
            cleanup
            ;;
        "urls")
            print_urls
            ;;
        "migrate")
            run_migrations
            ;;
        "rebuild-microservices")
            rebuild_microservices
            ;;

        "help"|*)
            echo "Использование: $0 [команда] [параметры]"
            echo
            echo "Команды:"
            echo "  start        - Запустить всю систему"
            echo "  stop         - Остановить всю систему"
            echo "  restart      - Перезапустить всю систему"
            echo "  build        - Собрать все сервисы"
            echo "  status       - Показать статус системы"
            echo "  health       - Проверить здоровье сервисов"
            echo "  logs [сервис] - Показать логи (всех или конкретного сервиса)"
            echo "  scale <сервис> <реплики> - Масштабировать сервис"
            echo "  cleanup      - Очистить систему (удалить контейнеры и тома)"
            echo "  urls         - Показать полезные URL"
            echo "  migrate      - Выполнить миграции БД"
            echo "  rebuild-microservices - Пересобрать и запустить только микросервисы"
            echo "  help         - Показать эту справку"
            echo
            echo "Примеры:"
            echo "  $0 start                    # Запустить всю систему"
            echo "  $0 logs api-gateway         # Показать логи API Gateway"
            echo "  $0 scale user-service 3     # Масштабировать до 3 реплик"
            ;;
    esac
}

# Обработка сигналов
trap 'log "INFO" "Получен сигнал прерывания, завершение..."; exit 0' INT TERM

# Запуск
main "$@"
