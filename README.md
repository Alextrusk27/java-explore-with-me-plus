# Explore With Me (Афиша)

Микросервисная платформа для создания событий, поиска участников, сбора статистики и комментирования.

| Файл | Описание |
|------|----------|
| [`ewm-main-service-spec.json`](ewm-main-service-spec.json) | OpenAPI спецификация основного сервиса |
| [`ewm-stats-service-spec.json`](ewm-stats-service-spec.json) | OpenAPI спецификация сервиса статистики |

##  👥 Команда

- [Nikolay Aleksandrov](https://github.com/Alextrusk27) — Team Lead, разработчик
- [Andrey Zavrichko](https://github.com/andreyzavrichko) — разработчик
- [Fsmnza](https://github.com/Fsmnza) — разработчик
- [munuprig](https://github.com/munuprig) — разработчик

## Maven-архитектура

```text
explore-with-me/                         # Parent POM
│
├── stats/                               # Сервис статистики
│   ├── dto/                             # Общие DTO
│   ├── client/                          # HTTP-клиент
│   └── server/                          # HTTP-сервер
│
└── main-service/                        # Основной сервис 
```

## 📊 Сервис статистики

Сервис на **Spring Web + JPA + PostgreSQL** для сбора просмотров событий (`GET /events` и `GET /events/{id}`). Предоставляет API записи хитов (`POST /hit`) и выгрузки статистики (`GET /stats`). Включает **HTTP-клиент (RestTemplate)** для интеграции с основным сервисом.

## 🎯 Основной сервис

Трёхуровневый REST API на **Spring Web + JPA** с ролевой моделью (PUBLIC/PRIVATE/ADMIN).

**PUBLIC** — поиск событий с фильтрацией и сортировкой (по просмотрам или дате). Каждый запрос логируется через **stats-client**.

**PRIVATE** — создание и редактирование событий, подача заявок на участие, комментарии.

**ADMIN** — модерация событий, CRUD категорий и подборок, управление пользователями.

Событие проходит жизненный цикл: создание → ожидание публикации → опубликовано / отменено.  

## 🛠 Технологический стек

| Технология | Назначение |
|------------|------------|
| **Java** | Основной язык |
| **Spring Boot** | Фреймворк |
| **Spring Web MVC** | REST API |
| **Spring Data JPA** | ORM + репозитории |
| **Query DSL** | Типобезопасные динамические запросы |
| **PostgreSQL** | База данных |
| **Spring Validation** | Валидация DTO |
| **MapStruct** | Маппинг Entity ↔ DTO |
| **RestTemplate** | HTTP-клиент |
| **Spring Boot Actuator** | Мониторинг |
| **Maven** | Сборка проекта |
| **Docker** | Контейнеризация |
| **Lombok** | Генерация кода |
| **Postman** | Тестирование API (коллекции в `/postman`) |

## 🚀 Запуск проекта в Docker

```bash
# 1. Клонировать репозиторий
git clone https://github.com/Alextrusk27/java-explore-with-me-plus.git
cd java-explore-with-me-plus

# 2. Собрать проект
mvn clean package

# 3. Запустить контейнеры
docker-compose up -d

# 4. Проверить работоспособность
curl http://localhost:8080/actuator/health   # Основной сервис
curl http://localhost:9090/actuator/health   # Сервис статистики
```