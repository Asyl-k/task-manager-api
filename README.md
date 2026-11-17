# Task Manager API

Учебный REST API для управления задачами на Spring Boot.

## Требования
- Java 21+
- Maven 3.9+
- Docker Desktop (необязательно)

## Запуск

```bash
mvn spring-boot:run
```

По умолчанию используется H2 in-memory. Swagger UI: http://localhost:8080/swagger-ui.html

Запуск с PostgreSQL:

```bash
docker compose up --build
```

## API

| Метод | Путь | Описание |
|---|---|---|
| GET | `/api/tasks` | Список; фильтр `?status=TODO` |
| GET | `/api/tasks/{id}` | Получить задачу |
| POST | `/api/tasks` | Создать задачу |
| PUT | `/api/tasks/{id}` | Обновить задачу |
| DELETE | `/api/tasks/{id}` | Удалить задачу |

Пример тела:

```json
{"title":"Изучить Spring Boot","description":"REST и слои приложения","status":"TODO"}
```

## Проверки

```bash
mvn test
mvn package
```

Стек: Java 21 · Spring Boot 3 · Spring Web · Spring Data JPA · PostgreSQL · H2 · Validation · OpenAPI · JUnit 5 · Docker.
