<div align="center">
  <h1>🚀 DevFlow</h1>
  <p><strong>Enterprise Task & Activity Tracking System</strong></p>
  <p>
    <em>A full-stack task management platform with automatic activity feed, JWT authentication, and role-based access control</em>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Java-21-blue?style=flat-square&logo=openjdk" alt="Java 21">
    <img src="https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?style=flat-square&logo=springboot" alt="Spring Boot 3.5">
    <img src="https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react" alt="React 18">
    <img src="https://img.shields.io/badge/TypeScript-5.4-3178C6?style=flat-square&logo=typescript" alt="TypeScript 5.4">
    <img src="https://img.shields.io/badge/PostgreSQL-15-4169E1?style=flat-square&logo=postgresql" alt="PostgreSQL 15">
    <img src="https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square&logo=docker" alt="Docker">
  </p>
</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [Database Schema (ERD)](#-database-schema-erd)
- [API Endpoints](#-api-endpoints)
- [Frontend Pages & Routes](#-frontend-pages--routes)
- [Key Technical Highlights](#-key-technical-highlights)
- [Quick Start with Docker](#-quick-start-with-docker)
- [Manual Setup (Development)](#-manual-setup-development)
- [Environment Variables](#-environment-variables)
- [Testing](#-testing)
- [API Usage Examples](#-api-usage-examples)
- [Contributing](#-contributing)
- [License](#-license)

---

## 📖 Overview

**DevFlow** is a modern, full-stack task management application designed for teams. It combines a **Spring Boot 3 RESTful API** with a **React + TypeScript frontend** to provide a seamless project management experience.

### What makes DevFlow special?

- **Automatic Activity Feed** — Like a social media newsfeed for your projects. Every task status change is automatically logged via AOP (Aspect-Oriented Programming), creating a rich audit trail without any manual logging code.
- **JWT with Token Rotation** — Stateless authentication with short-lived access tokens (15 min) and rotating refresh tokens (7 days) for security and scalability.
- **Role-Based Access Control** — Two roles (`USER` and `ADMIN`) with granular permissions. Admins can manage any project; users own their projects.
- **Optimistic Locking** — Tasks use JPA `@Version` for optimistic locking to prevent concurrent modification conflicts.
- **Dockerized from Day One** — One command to spin up the entire stack: PostgreSQL, Spring Boot backend, and React frontend behind Nginx.

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser                              │
│                  React + TypeScript + Chakra UI              │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP / API calls
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Nginx (Reverse Proxy)                     │
│              Serves static files + proxies /api              │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot 3 Backend (Port 8080)               │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ JWT Auth     │  │ Controllers  │  │ AOP Aspect       │   │
│  │ Filter       │─▶│ (REST API)   │─▶│ (Auto Logging)   │   │
│  └──────────────┘  └──────┬───────┘  └──────────────────┘   │
│                           │                                  │
│                    ┌──────▼───────┐                          │
│                    │   Services   │                          │
│                    │  (Business   │                          │
│                    │   Logic)     │                          │
│                    └──────┬───────┘                          │
│                           │                                  │
│                    ┌──────▼───────┐                          │
│                    │  Repositories│                          │
│                    │  (Spring     │                          │
│                    │   Data JPA)  │                          │
│                    └──────┬───────┘                          │
└───────────────────────────┼──────────────────────────────────┘
                            │
                            ▼
              ┌─────────────────────────┐
              │     PostgreSQL 15        │
              │  (Docker Container)      │
              └─────────────────────────┘
```

### Request Flow

```
HTTP Request
    │
    ▼
JwtAuthenticationFilter ──── No token? ──▶ Continue (public endpoints)
    │
    ▼ (Has Bearer token)
Validate JWT ──── Invalid? ──▶ 401 Unauthorized
    │
    ▼ (Valid token)
Set SecurityContext
    │
    ▼
Controller (REST endpoint)
    │
    ▼
Service Interface → ServiceImpl (business logic + authorization)
    │
    ▼
Repository (Spring Data JPA)
    │
    ▼
PostgreSQL
```

---

## 🛠 Tech Stack

### Backend

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.x |
| **Security** | Spring Security 6, JWT (jjwt 0.12.x) |
| **Database** | PostgreSQL 15, Spring Data JPA, Hibernate |
| **API Documentation** | Swagger UI (springdoc-openapi 2.8.x) |
| **Build Tool** | Maven (Wrapper included) |
| **Testing** | JUnit 5, Testcontainers, Spring Security Test |
| **Others** | Lombok, AOP (AspectJ), Bean Validation (Jakarta) |

### Frontend

| Layer | Technology |
|-------|-----------|
| **Language** | TypeScript 5.4 |
| **UI Library** | React 18 |
| **Build Tool** | Vite 5 |
| **UI Components** | Chakra UI 2.x |
| **HTTP Client** | Axios (with interceptors for auth & refresh) |
| **Routing** | React Router v6 |
| **Notifications** | react-hot-toast |
| **Animation** | Framer Motion |

### DevOps

| Tool | Purpose |
|------|---------|
| **Docker** | Containerization |
| **Docker Compose** | Multi-container orchestration |
| **Nginx** | Reverse proxy & static file serving |
| **Multi-stage Builds** | Optimized Docker images (~180MB backend) |

---

## ✨ Features

### 🔐 Authentication & Authorization
- User registration with BCrypt password hashing
- JWT-based stateless authentication (access + refresh tokens)
- Token rotation for refresh tokens (old tokens become invalid)
- Role-based access control: `USER` and `ADMIN`
- Automatic 401/403 error handling

### 📁 Project Management
- Create, read, update, delete projects
- Owner-based access control (only owner or admin can modify)
- Paginated project listing
- Admin can view and manage all projects

### ✅ Task Management
- Full CRUD operations within projects
- Task status workflow: `TODO` → `IN_PROGRESS` → `DONE`
- Priority levels: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- Assign tasks to users
- Filter tasks by status
- Optimistic locking with `@Version` for concurrent safety
- Paginated task listing

### 📊 Activity Feed
- Automatic activity logging via AOP (AspectJ)
- Logs created on: task creation, task status changes
- Per-project activity feed (paginated, newest first)
- System-wide activity logs for admins
- Visual timeline UI component

### 🖥 Modern Frontend
- Responsive sidebar layout with Chakra UI
- Dashboard with project statistics (total, TODO, in-progress, done)
- Real-time status updates via dropdown
- Visual status badges and priority tags
- Activity timeline with visual connectors
- Admin panel for system-wide logs
- Token refresh interceptor (seamless re-authentication)

---

## 📁 Project Structure

```
devflow/
├── docker-compose.yml              # Multi-container orchestration
├── README.md                       # You are here
│
├── backend/                        # Spring Boot Application
│   ├── Dockerfile                  # Multi-stage Docker build
│   ├── pom.xml                     # Maven dependencies
│   ├── .env.example                # Environment variable template
│   └── src/
│       ├── main/
│       │   ├── java/com/example/devflow/
│       │   │   ├── DevflowApplication.java
│       │   │   ├── config/
│       │   │   │   ├── CorsConfig.java           # CORS for dev server
│       │   │   │   ├── CustomUserDetailsService.java
│       │   │   │   ├── JacksonConfig.java        # ISO-8601 UTC dates
│       │   │   │   ├── JwtAuthenticationFilter.java  # Token validation
│       │   │   │   ├── JwtUtil.java              # Token generation
│       │   │   │   ├── SecurityConfig.java       # Spring Security
│       │   │   │   └── SwaggerConfig.java        # OpenAPI + JWT support
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java       # /api/auth/**
│       │   │   │   ├── ProjectController.java    # /api/projects/**
│       │   │   │   ├── TaskController.java       # /api/projects/{id}/tasks/**
│       │   │   │   ├── ActivityController.java   # /api/projects/{id}/activities/**
│       │   │   │   └── AdminController.java      # /api/admin/**
│       │   │   ├── dto/
│       │   │   │   ├── request/                  # Request DTOs
│       │   │   │   │   ├── RegisterRequest.java
│       │   │   │   │   ├── LoginRequest.java
│       │   │   │   │   ├── RefreshTokenRequest.java
│       │   │   │   │   ├── CreateProjectRequest.java
│       │   │   │   │   ├── CreateTaskRequest.java
│       │   │   │   │   └── UpdateTaskStatusRequest.java
│       │   │   │   └── response/                 # Response DTOs
│       │   │   │       ├── ApiResponse.java      # Generic wrapper
│       │   │   │       ├── AuthResponse.java
│       │   │   │       ├── ProjectResponse.java
│       │   │   │       ├── TaskResponse.java
│       │   │   │       └── ActivityLogResponse.java
│       │   │   ├── entity/
│       │   │   │   ├── User.java                 # Implements UserDetails
│       │   │   │   ├── Project.java
│       │   │   │   ├── Task.java                 # With @Version lock
│       │   │   │   └── ActivityLog.java
│       │   │   ├── exception/
│       │   │   │   ├── BusinessException.java
│       │   │   │   ├── ResourceNotFoundException.java
│       │   │   │   ├── AccessDeniedException.java
│       │   │   │   └── GlobalExceptionHandler.java  # Centralized error handling
│       │   │   ├── model/
│       │   │   │   ├── Role.java                 # ROLE_USER, ROLE_ADMIN
│       │   │   │   ├── TaskStatus.java           # TODO, IN_PROGRESS, DONE
│       │   │   │   └── TaskPriority.java         # LOW, MEDIUM, HIGH, CRITICAL
│       │   │   ├── repository/                   # Spring Data JPA
│       │   │   │   ├── UserRepository.java
│       │   │   │   ├── ProjectRepository.java
│       │   │   │   ├── TaskRepository.java
│       │   │   │   └── ActivityLogRepository.java
│       │   │   ├── service/
│       │   │   │   ├── AuthService.java
│       │   │   │   ├── ProjectService.java
│       │   │   │   ├── TaskService.java
│       │   │   │   ├── ActivityLogService.java
│       │   │   │   └── impl/                     # Implementations
│       │   │   └── aspect/
│       │   │       └── LoggingAspect.java        # AOP auto-logging
│       │   └── resources/
│       │       └── application.yaml              # Config with env vars
│       └── test/                                 # Unit & integration tests
│           ├── java/com/example/devflow/
│           │   ├── DevflowApplicationTests.java
│           │   ├── DevflowIntegrationTest.java
│           │   ├── config/TestSecurityConfig.java
│           │   ├── controller/
│           │   │   ├── AuthControllerTest.java
│           │   │   ├── ProjectControllerTest.java
│           │   │   ├── TaskControllerTest.java
│           │   │   └── ActivityControllerTest.java
│           │   └── service/impl/
│           │       ├── AuthServiceImplTest.java
│           │       ├── ProjectServiceImplTest.java
│           │       ├── TaskServiceImplTest.java
│           │       └── ActivityLogServiceImplTest.java
│           └── resources/
│               └── application-integration.yaml
│
└── frontend/                       # React + TypeScript Application
    ├── Dockerfile                  # Multi-stage build (Node → Nginx)
    ├── nginx.conf                  # Nginx config for SPA + API proxy
    ├── package.json
    ├── vite.config.ts              # Vite config with API proxy
    ├── tsconfig.json
    └── src/
        ├── main.tsx                # Entry point
        ├── App.tsx                 # Routes & providers
        ├── api/
        │   └── client.ts           # Axios instance with interceptors
        ├── context/
        │   └── AuthContext.tsx      # Auth state management
        ├── types/
        │   └── index.ts            # TypeScript interfaces
        ├── utils/
        │   └── formatDate.ts       # UTC date formatting
        ├── components/
        │   ├── AppLayout.tsx       # Sidebar + header layout
        │   ├── ProtectedRoute.tsx  # Auth guard
        │   ├── ProjectForm.tsx     # Create/Edit project form
        │   ├── TaskForm.tsx        # Create/Edit task form
        │   ├── StatusBadge.tsx     # Color-coded status badge
        │   ├── PriorityTag.tsx     # Color-coded priority tag
        │   ├── StatCard.tsx        # Dashboard stat card
        │   └── ActivityTimeline.tsx # Visual timeline component
        └── pages/
            ├── LoginPage.tsx
            ├── RegisterPage.tsx
            ├── DashboardPage.tsx   # Overview with stats
            ├── ProjectListPage.tsx # Paginated project list
            ├── CreateProjectPage.tsx
            ├── ProjectDetailPage.tsx # Project + tasks view
            ├── CreateTaskPage.tsx
            ├── TaskDetailPage.tsx  # Task with status update
            ├── TaskListPage.tsx    # Filterable task table
            ├── ActivityFeedPage.tsx # Per-project activity
            └── SystemLogsPage.tsx  # Admin system logs
```

---

## 🗄 Database Schema (ERD)

```
┌──────────────────┐       ┌──────────────────────┐
│      users       │       │      projects         │
├──────────────────┤       ├──────────────────────┤
│ id (PK)          │◄──────│ owner_id (FK)         │
│ username (UNIQUE)│       │ id (PK)               │
│ password (bcrypt)│       │ name                  │
│ role (ENUM)      │       │ description           │
└──────────────────┘       └──────────┬───────────┘
        │                             │
        │                             │
        │                     ┌───────┴────────────┐
        │                     │       tasks         │
        │                     ├────────────────────┤
        │                     │ id (PK)             │
        │                     │ project_id (FK)     │
        └─────────────────────│ assignee_id (FK)    │
                              │ title               │
                              │ description         │
                              │ status (ENUM)       │
                              │ priority (ENUM)     │
                              │ version (@Version)  │
                              └──────────┬─────────┘
                                         │
                                  ┌──────┴──────────────┐
                                  │    activity_logs     │
                                  ├─────────────────────┤
                                  │ id (PK)              │
                                  │ project_id (FK)      │
                                  │ content              │
                                  │ created_at (UTC)     │
                                  └─────────────────────┘
```

### Indexes
- `tasks`: `idx_tasks_status` (status), `idx_tasks_project` (project_id)
- `activity_logs`: `idx_logs_project` (project_id), `idx_logs_created` (created_at)

---

## 🌐 API Endpoints

### Authentication (No Token Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register a new user (returns access + refresh tokens) |
| `POST` | `/api/auth/login` | Authenticate and get JWT tokens |
| `POST` | `/api/auth/refresh` | Exchange refresh token for new tokens (rotation) |

### Projects (Token Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/projects` | List all projects (paginated) |
| `POST` | `/api/projects` | Create a new project |
| `GET` | `/api/projects/{id}` | Get project by ID |
| `PUT` | `/api/projects/{id}` | Update project (owner/admin only) |
| `DELETE` | `/api/projects/{id}` | Delete project (owner/admin only) |

### Tasks (Token Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/projects/{id}/tasks` | List tasks (paginated, filter by `?status=`) |
| `POST` | `/api/projects/{id}/tasks` | Create a task (owner/admin only) |
| `GET` | `/api/projects/{id}/tasks/{taskId}` | Get task by ID |
| `PUT` | `/api/projects/{id}/tasks/{taskId}` | Update task (owner/admin only) |
| `PATCH` | `/api/projects/{id}/tasks/{taskId}/status` | Update task status (triggers AOP activity log) |
| `DELETE` | `/api/projects/{id}/tasks/{taskId}` | Delete task (owner/admin only) |

### Activities (Token Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/projects/{id}/activities` | List activity logs (paginated, newest first) |

### Admin (Token Required, ADMIN Role)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/activities` | Get ALL system activity logs (paginated) |

### API Documentation

Once running, Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```

---

## 🖥 Frontend Pages & Routes

| Route | Page | Description |
|-------|------|-------------|
| `/login` | LoginPage | User sign-in |
| `/register` | RegisterPage | User registration |
| `/` | DashboardPage | Overview with project stats |
| `/projects` | ProjectListPage | Paginated project list |
| `/projects/new` | CreateProjectPage | Create a new project |
| `/projects/:id` | ProjectDetailPage | Project details + tasks |
| `/projects/:id/tasks/new` | CreateTaskPage | Create a task |
| `/projects/:id/tasks/:taskId` | TaskDetailPage | Task details + status update |
| `/projects/:id/activities` | ActivityFeedPage | Per-project activity timeline |
| `/admin/logs` | SystemLogsPage | Admin system-wide logs |

---

## 🔑 Key Technical Highlights

### 1. JWT Stateless Authentication with Token Rotation
- **Access tokens** expire in 15 minutes (configurable) — short-lived for security
- **Refresh tokens** expire in 7 days (configurable) — long-lived for convenience
- **Token rotation**: Each refresh issues a new refresh token, invalidating the old one
- No session state — horizontally scalable across multiple instances

### 2. AOP-Powered Automatic Activity Logging
- Uses Spring AOP `@AfterReturning` advice — fires only on successful operations
- Automatically logs task creation and status changes
- The service layer never calls `ActivityLogService` manually — the aspect handles it
- `@AfterReturning` chosen over `@Around` for simplicity and safety

### 3. Object-Level Authorization
- `401 Unauthorized` — missing or invalid JWT token
- `403 Forbidden` — valid token but not the resource owner
- `ADMIN` role bypasses all ownership checks
- Consistent authorization pattern across all services

### 4. Optimistic Locking with `@Version`
- Tasks use JPA `@Version` annotation for optimistic locking
- Prevents lost updates when multiple users modify the same task concurrently
- Throws `OptimisticLockException` on conflict

### 5. Consistent API Response Format
- Every endpoint returns `ApiResponse<T>`: `{ success, message, data }`
- Clients handle success/failure uniformly without parsing HTTP status codes
- `@JsonInclude(NON_NULL)` — null fields are omitted from responses

### 6. Centralized Exception Handling
- `@RestControllerAdvice` catches all exceptions
- Converts every error to consistent `ApiResponse` format
- Stack traces never exposed in production responses
- Handles validation errors, bad credentials, malformed JSON, etc.

### 7. Docker Multi-Stage Builds
- **Backend**: Maven builds JAR → lightweight JRE runtime image (~180MB)
- **Frontend**: Node builds static files → Nginx serves them
- **Nginx** also proxies `/api` requests to the backend (single origin in production)

### 8. Frontend Token Refresh Interceptor
- Axios interceptor detects 401 responses
- Automatically attempts token refresh before retrying the request
- Queues concurrent requests during refresh to avoid race conditions
- Falls back to logout if refresh fails

---

## 🚀 Quick Start with Docker

**Prerequisites:** [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/) installed.

```bash
# 1. Clone the repository
git clone https://github.com/anhtuan74k5/devflow.git
cd devflow

# 2. Start everything with one command
docker compose up -d --build
```

Once running:

| Service | URL |
|---------|-----|
| **Frontend** | [http://localhost:5173](http://localhost:5173) |
| **Backend API** | [http://localhost:8080](http://localhost:8080) |
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |

```bash
# Stop all services
docker compose down

# Stop and remove volumes (deletes database data)
docker compose down -v
```

---

## 🔧 Manual Setup (Development)

### Prerequisites
- Java 21+
- Node.js 20+
- PostgreSQL 15+
- Maven (or use `./mvnw` wrapper)

### Backend Setup

```bash
cd backend

# Copy environment variables
cp .env.example .env
# Edit .env with your PostgreSQL credentials

# Run with Maven
./mvnw spring-boot:run
```

### Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server (with API proxy to localhost:8080)
npm run dev
```

The frontend dev server runs at `http://localhost:5173` and proxies `/api` requests to `http://localhost:8080`.

---

## 🔐 Environment Variables

### Backend (`backend/.env` or Docker Compose)

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `devflow` | Database name |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `123` | Database password |
| `JWT_SECRET` | *(see below)* | JWT signing secret (≥256 bits for HS256) |
| `JWT_EXPIRATION_MS` | `86400000` | Access token expiry (default 24h, recommended 15min=900000) |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` | Refresh token expiry (default 7 days) |

> **⚠️ Security Note:** Change the default `JWT_SECRET` in production! Use a cryptographically random string of at least 256 bits (32 characters).

### Frontend (`frontend/.env`)

| Variable | Default | Description |
|----------|---------|-------------|
| *(none required)* | — | API proxy configured in `vite.config.ts` |

---

## 🧪 Testing

### Backend Tests

The backend includes both **unit tests** (with mocked dependencies) and **integration tests** (with Testcontainers for PostgreSQL).

```bash
cd backend

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthControllerTest

# Run integration tests only
./mvnw test -Dtest=DevflowIntegrationTest
```

### Test Coverage

| Test Class | Type | What it tests |
|------------|------|---------------|
| `AuthControllerTest` | Unit | Registration, login, refresh endpoints |
| `ProjectControllerTest` | Unit | CRUD operations, authorization |
| `TaskControllerTest` | Unit | CRUD, status updates, filtering |
| `ActivityControllerTest` | Unit | Activity log retrieval |
| `AuthServiceImplTest` | Unit | Business logic, validation, edge cases |
| `ProjectServiceImplTest` | Unit | Owner checks, admin bypass |
| `TaskServiceImplTest` | Unit | Task operations, optimistic locking |
| `ActivityLogServiceImplTest` | Unit | Log creation, access control |
| `DevflowIntegrationTest` | Integration | Full flow with real PostgreSQL (Testcontainers) |

---

## 📡 API Usage Examples

### Using curl

```bash
# ─── 1. Register a new user ───
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}'

# ─── 2. Login ───
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}' | jq -r '.data.token')

# ─── 3. Create a project ───
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"My Project","description":"First project"}'

# ─── 4. Create a task ───
curl -X POST http://localhost:8080/api/projects/1/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Fix login bug","description":"Investigate and fix","priority":"HIGH"}'

# ─── 5. Update task status (triggers AOP activity log) ───
curl -X PATCH http://localhost:8080/api/projects/1/tasks/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"status":"DONE"}'

# ─── 6. View activity logs ───
curl http://localhost:8080/api/projects/1/activities \
  -H "Authorization: Bearer $TOKEN"

# ─── 7. Refresh token ───
REFRESH_TOKEN="your-refresh-token-here"
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
```

### Example Response Format

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 1,
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "alice",
    "role": "ROLE_USER"
  }
}
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

### Development Guidelines

- Follow existing code style and patterns
- Write tests for new functionality
- Update API documentation (Swagger annotations) for new endpoints
- Keep the AOP aspect as the single source of activity logging
- Use `AuthService.getCurrentUser()` for accessing the authenticated user

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">
  <p>Built with ❤️ using Spring Boot & React</p>
  <p>
    <a href="https://github.com/anhtuan74k5/devflow">GitHub Repository</a>
  </p>
</div>
