# DevFlow — Enterprise Task & Activity Tracking API

![Java 21](https://img.shields.io/badge/Java-21-blue)
![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED)

A RESTful API for team task management with automatic activity feed (like a social media newsfeed). Built with Spring Boot 3, JWT authentication, and PostgreSQL.

---

## Tech Stack

| Layer        | Technology                                      |
|-------------|-------------------------------------------------|
| Language    | Java 21                                         |
| Framework   | Spring Boot 3.x, Spring Security 6, Spring Data JPA |
| Database    | PostgreSQL 15                                   |
| Auth        | JWT (jjwt 0.12.x), BCrypt                       |
| API Docs    | Swagger UI (springdoc-openapi 2.8.6)            |
| Build       | Maven, Docker multi-stage build                 |
| Others      | Lombok, AOP (AspectJ), Bean Validation          |

---

## Architecture Overview

**Request flow:** HTTP Request → JwtAuthenticationFilter (validate token) → Controller → Service (interface) → ServiceImpl (business logic + authorization) → Repository (JPA) → PostgreSQL

**Package structure:**
```
com.example.devflow
├── config        → Security, JWT, Swagger, UserDetailsService
├── controller    → Auth, Project, Task, Activity endpoints
├── entity        → User, Project, Task, ActivityLog
├── repository    → JPA repositories with @EntityGraph
├── service
│   ├── (interface)  → AuthService, ProjectService, TaskService, ActivityLogService
│   └── impl         → Implementation classes
├── dto
│   ├── request   → RegisterRequest, LoginRequest, CreateProjectRequest, CreateTaskRequest, UpdateTaskStatusRequest
│   └── response  → ApiResponse<T>, AuthResponse, ProjectResponse, TaskResponse, ActivityLogResponse
├── exception     → GlobalExceptionHandler + custom exceptions
└── aspect        → LoggingAspect (AOP auto activity logging)
```

---

## ERD

```
┌─────────────┐       ┌──────────────┐
│    users    │       │   projects   │
├─────────────┤       ├──────────────┤
│ id (PK)     │◄──────│ owner_id (FK)│
│ username    │       │ id (PK)      │
│ password    │       │ name         │
│ role        │       │ description  │
└─────────────┘       └──────┬───────┘
       │                     │
       │                     │
       │              ┌──────┴────────┐
       │              │    tasks      │
       │              ├───────────────┤
       │              │ id (PK)       │
       │              │ project_id(FK)│
       └──────────────│ assignee_id   │
                      │ title         │
                      │ description   │
                      │ status (ENUM) │
                      │ priority(ENUM)│
                      │ version       │
                      └──────┬────────┘
                             │
                      ┌──────┴──────────┐
                      │  activity_logs  │
                      ├─────────────────┤
                      │ id (PK)         │
                      │ project_id (FK) │
                      │ content         │
                      │ created_at      │
                      └─────────────────┘
```

---

## API Endpoints

### Authentication (no token required)

| Method | Endpoint              | Description                    |
|--------|-----------------------|--------------------------------|
| POST   | `/api/auth/register`  | Register a new user            |
| POST   | `/api/auth/login`     | Authenticate and get JWT token |

### Projects (token required)

| Method | Endpoint               | Description                          |
|--------|------------------------|--------------------------------------|
| GET    | `/api/projects`        | List all projects (paginated)        |
| POST   | `/api/projects`        | Create a new project                 |
| GET    | `/api/projects/{id}`   | Get project by ID                    |
| PUT    | `/api/projects/{id}`   | Update project (owner only)          |
| DELETE | `/api/projects/{id}`   | Delete project (owner only)          |

### Tasks (token required)

| Method | Endpoint                                          | Description                              |
|--------|---------------------------------------------------|------------------------------------------|
| GET    | `/api/projects/{id}/tasks`                        | List tasks (paginated, filter by ?status) |
| POST   | `/api/projects/{id}/tasks`                        | Create a task (owner only)               |
| GET    | `/api/projects/{id}/tasks/{taskId}`               | Get task by ID                           |
| PUT    | `/api/projects/{id}/tasks/{taskId}`               | Update task (owner only)                 |
| PATCH  | `/api/projects/{id}/tasks/{taskId}/status`        | Update task status (triggers activity log)|
| DELETE | `/api/projects/{id}/tasks/{taskId}`               | Delete task (owner only)                 |

### Activities (token required)

| Method | Endpoint                                          | Description                              |
|--------|---------------------------------------------------|------------------------------------------|
| GET    | `/api/projects/{id}/activities`                   | List activity logs (paginated, newest first) |

---

## Key Technical Highlights

1. **JWT Stateless Authentication** — Every request is authenticated via Bearer token. No session state, making the API horizontally scalable.

2. **Object-level Authorization** — Returns `401 Unauthorized` for missing/invalid tokens and `403 Forbidden` for valid tokens accessing resources the user doesn't own.

3. **AOP Automatic Activity Logging** — Task status changes are automatically logged via `@AfterReturning` aspect. The service layer never calls `ActivityLogService` manually — the aspect intercepts `updateTaskStatus()` and creates the log entry.

4. **Docker Multi-stage Build** — The Dockerfile uses a two-stage build: Maven builds the JAR in stage 1, then a lightweight JRE image runs it in stage 2. Final image is ~180MB.

---

## Quick Start

**Prerequisites:** Docker and Docker Compose installed.

```bash
# Clone the repository
git clone https://github.com/anhtuan74k5/devflow.git
cd devflow

# Start everything with one command
docker compose up -d --build
```

The API will be available at `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Quick Test with curl

```bash
# 1. Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}'

# 2. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}' | jq -r '.data.token')

# 3. Create a project
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"My Project","description":"First project"}'

# 4. Create a task
curl -X POST http://localhost:8080/api/projects/1/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Fix login bug","description":"Investigate and fix","priority":"HIGH"}'

# 5. Update task status (triggers activity log via AOP)
curl -X PATCH http://localhost:8080/api/projects/1/tasks/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"status":"DONE"}'

# 6. View activity logs
curl http://localhost:8080/api/projects/1/activities \
  -H "Authorization: Bearer $TOKEN"
```

---

## Environment Variables

| Variable            | Default                                                              | Description                    |
|---------------------|----------------------------------------------------------------------|--------------------------------|
| `DB_HOST`           | `localhost`                                                          | PostgreSQL host                |
| `DB_PORT`           | `5432`                                                               | PostgreSQL port                |
| `DB_NAME`           | `devflow`                                                            | Database name                  |
| `DB_USERNAME`       | `postgres`                                                           | Database username              |
| `DB_PASSWORD`       | `123`                                                                | Database password              |
| `JWT_SECRET`        | `devflow-secret-key-must-be-at-least-256-bits-long-for-hs256`        | JWT signing secret (≥256 bits) |
| `JWT_EXPIRATION_MS` | `86400000`                                                           | JWT token expiry (24h)         |
