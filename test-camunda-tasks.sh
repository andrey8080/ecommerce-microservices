#!/bin/bash

echo "=== Тестирование Camunda Tasklist ==="

# Проверяем доступность процессов
echo "1. Получаем определения процессов:"
curl -s "http://localhost:8081/camunda/api/engine/engine/default/process-definition" | jq -r '.[] | "ID: \(.id), Key: \(.key), Name: \(.name)"' || curl -s "http://localhost:8081/camunda/api/engine/engine/default/process-definition"

echo -e "\n2. Получаем экземпляры процессов:"
curl -s "http://localhost:8081/camunda/api/engine/engine/default/process-instance" | jq -r '.[] | "ID: \(.id), Process: \(.processDefinitionId)"' || curl -s "http://localhost:8081/camunda/api/engine/engine/default/process-instance"

echo -e "\n3. Получаем все задачи:"
curl -s "http://localhost:8081/camunda/api/engine/engine/default/task" | jq -r '.[] | "Task ID: \(.id), Name: \(.name), Assignee: \(.assignee)"' || curl -s "http://localhost:8081/camunda/api/engine/engine/default/task"

echo -e "\n4. Запускаем новый процесс userRegistrationProcess:"
INSTANCE_ID=$(curl -s -X POST "http://localhost:8081/camunda/api/engine/engine/default/process-definition/key/userRegistrationProcess/start" \
  -H "Content-Type: application/json" \
  -d '{}' | jq -r '.id' 2>/dev/null || echo "FAILED")

if [ "$INSTANCE_ID" != "FAILED" ] && [ "$INSTANCE_ID" != "null" ]; then
    echo "Новый экземпляр создан с ID: $INSTANCE_ID"
    
    echo -e "\n5. Получаем задачи для нового экземпляра:"
    curl -s "http://localhost:8081/camunda/api/engine/engine/default/task?processInstanceId=$INSTANCE_ID" | jq || curl -s "http://localhost:8081/camunda/api/engine/engine/default/task?processInstanceId=$INSTANCE_ID"
else
    echo "Не удалось создать новый экземпляр процесса"
fi

echo -e "\n6. Получаем задачи для пользователя demo:"
curl -s "http://localhost:8081/camunda/api/engine/engine/default/task?candidateUser=demo" | jq || curl -s "http://localhost:8081/camunda/api/engine/engine/default/task?candidateUser=demo"

echo -e "\n7. Получаем группы пользователя demo:"
curl -s "http://localhost:8081/camunda/api/engine/engine/default/group?member=demo" | jq || curl -s "http://localhost:8081/camunda/api/engine/engine/default/group?member=demo"
