# Руководство по запуску процесса userRegistrationProcess в Camunda

## Способ 1: Через веб-интерфейс Camunda Tasklist

### Шаг 1: Откройте Camunda Tasklist
Откройте браузер и перейдите по адресу: `http://localhost:8081/camunda/app/tasklist/`

### Шаг 2: Авторизация
- **Логин:** demo
- **Пароль:** demo

### Шаг 3: Запуск нового процесса
1. В Tasklist нажмите кнопку "Start process" (Запустить процесс)
2. Найдите и выберите процесс "User Registration" (userRegistrationProcess)
3. Нажмите "Start" для запуска экземпляра процесса

### Шаг 4: Выполнение пользовательской задачи
1. После запуска процесса появится задача "Enter Details" (Заполнить детали)
2. Кликните на задачу для её выполнения
3. Заполните форму регистрации пользователя
4. Нажмите "Complete" для завершения задачи

## Способ 2: Через REST API

### Запуск процесса через curl:

```bash
# Запуск нового экземпляра процесса
curl -X POST \
  http://localhost:8081/camunda/api/engine/engine/default/process-definition/key/userRegistrationProcess/start \
  -H "Content-Type: application/json" \
  -u demo:demo \
  -d '{
    "variables": {
      "username": {"value": "testuser", "type": "String"},
      "email": {"value": "test@example.com", "type": "String"}
    }
  }'
```

### Получение списка активных задач:
```bash
curl -u demo:demo \
  http://localhost:8081/camunda/api/engine/engine/default/task?processDefinitionKey=userRegistrationProcess
```

### Выполнение пользовательской задачи:
```bash
# Заменить {taskId} на реальный ID задачи
curl -X POST \
  http://localhost:8081/camunda/api/engine/engine/default/task/{taskId}/complete \
  -H "Content-Type: application/json" \
  -u demo:demo \
  -d '{
    "variables": {
      "fullName": {"value": "Test User", "type": "String"},
      "age": {"value": 25, "type": "Integer"}
    }
  }'
```

## Способ 3: Через Camunda Cockpit

### Мониторинг процессов:
1. Откройте `http://localhost:8081/camunda/app/cockpit/`
2. Войдите с логином demo/demo
3. Переходите в "Process Definitions" для просмотра определений процессов
4. Кликните на "userRegistrationProcess" для просмотра активных экземпляров

## Описание процесса

Процесс `userRegistrationProcess` состоит из следующих шагов:

1. **Start Event** - Начальное событие, запускающее процесс
2. **User Task "Enter Details"** - Пользовательская задача для ввода данных регистрации
   - Назначена группе "admin"
   - Использует HTML форму для ввода данных
3. **Service Task "Create User"** - Сервисная задача для создания пользователя
   - Выполняется делегатом `CreateUserDelegate`
4. **End Event** - Завершающее событие процесса

## Проверка статуса

### Проверка доступности сервиса:
```bash
curl http://localhost:8081/actuator/health
```

### Проверка задеплоенных процессов:
```bash
curl -u demo:demo \
  http://localhost:8081/camunda/api/engine/engine/default/process-definition
```

## Возможные проблемы и решения

1. **401 Unauthorized** - Проверьте логин/пароль demo/demo
2. **404 Not Found** - Убедитесь, что сервис запущен и процесс задеплоен
3. **500 Internal Server Error** - Проверьте логи сервиса в Docker

### Просмотр логов:
```bash
docker-compose logs user-service
```
