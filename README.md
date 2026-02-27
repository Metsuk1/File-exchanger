# File Exchanger

A full-stack file sharing platform built on a **custom Java HTTP server** (no Spring Boot), **React 19** frontend, **PostgreSQL**, **MinIO** object storage, and **Nginx** reverse proxy — fully containerized with Docker.

> **Live demo:** [http://85.198.88.69](http://85.198.88.69)

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Testing](#testing)
- [CI/CD](#cicd)
- [Project Structure](#project-structure)
- [License](#license)

---

## Features

- **User Authentication** — Registration, login, and JWT-based session management (HS256, 24 h expiry)
- **File Management** — Upload, download, list, and delete files (up to 200 MB)
- **Shareable Links** — Generate public download links for any file via unique tokens
- **User Profiles** — View and edit profile information
- **Object Storage** — Files stored in MinIO (S3-compatible), decoupled from the application server
- **Security** — BCrypt password hashing, input validation, path traversal protection, security headers via Nginx
- **Containerized** — Four-service Docker Compose stack (PostgreSQL, MinIO, backend, Nginx)
- **CI/CD** — Automated testing on PR, one-click deployment to VPS

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 22, Custom HTTP Server (ServerSocket + reflection-based routing) |
| **Frontend** | React 19, React Router 7, Axios |
| **Database** | PostgreSQL 16, HikariCP, Liquibase migrations |
| **Storage** | MinIO (S3-compatible object storage) |
| **Auth** | JWT (JJWT 0.12.6, HMAC-SHA256), BCrypt |
| **Proxy** | Nginx 1.27 (gzip, security headers, static file serving) |
| **Infrastructure** | Docker, Docker Compose |
| **Testing** | JUnit 5, Mockito, AssertJ, Gatling (load tests) |
| **Code Quality** | JaCoCo (80% line / 60% branch coverage), Spotless (Palantir Java Format) |

---

## Architecture

```
┌──────────────┐       ┌──────────────────┐       ┌────────────────┐
│   Browser    │──────▶│  Nginx :80/:443  │──────▶│  Backend :8080 │
│  (React SPA) │◀──────│  reverse proxy   │◀──────│  Custom HTTP   │
└──────────────┘       └──────────────────┘       └───────┬────────┘
                        serves /static                    │
                        proxies /api/*                    │
                                                ┌────────┴────────┐
                                                │                 │
                                          ┌─────▼──────┐   ┌─────▼──────┐
                                          │ PostgreSQL  │   │   MinIO    │
                                          │   :5432     │   │   :9000    │
                                          └────────────┘   └────────────┘
```

### Custom Web Server

The backend is a **from-scratch HTTP server** — no Spring, no Netty, no embedded Tomcat. Key components:

| Component | Responsibility |
|---|---|
| `CustomWebServer` | Accepts connections via `ServerSocket`, manages a configurable thread pool (200 threads, virtual thread support) |
| `RouteRegistry` | Scans controllers at startup via reflection, builds a routing table from custom annotations |
| `HttpRequestParser` | Parses raw HTTP requests including multipart/form-data |
| `Router` | Matches incoming requests to handler methods (supports path variables, query params) |
| `ParameterBinder` | Maps request data to method parameters using annotations (`@CustomRequestBody`, `@CustomRequestParam`, etc.) |
| `ResponseConverter` | Serializes responses — `String` → `text/plain`, objects → `application/json`, streams → binary |
| `RequestDispatcher` | Orchestrates the full request lifecycle with structured error handling |

### Custom Annotations

The framework provides Spring-like annotations implemented from scratch:

```java
@CustomRestController
@CustomRequestMapping("/api/v1/files")
public class FileController {

    @CustomPostMapping("/upload")
    public FileDto upload(@CustomRequestHeader("Authorization") String auth,
                          @CustomRequestPart("file") InputStream file) { ... }

    @CustomGetMapping("/download")
    public InputStream download(@CustomRequestHeader("Authorization") String auth,
                                @CustomRequestParam("fileId") Long fileId) { ... }

    @CustomGetMapping("/public/{token}")
    public InputStream publicDownload(@CustomPathVariable("token") String token) { ... }
}
```

---

## Getting Started

### Prerequisites

- **Docker** and **Docker Compose** (for containerized setup)
- **Java 22** and **Maven 3.9+** (for local backend development)
- **Node.js 20+** and **npm** (for local frontend development)

### Quick Start (Docker)

1. **Clone the repository**

```bash
git clone https://github.com/<your-username>/File-exchanger.git
cd File-exchanger
```

2. **Configure environment variables**

```bash
cp .env.example .env
# Edit .env and set secure values for all secrets
```

Required variables:

| Variable | Description |
|---|---|
| `JWT_SECRET` | Signing key for JWT tokens (min 32 characters) |
| `POSTGRES_DB` | PostgreSQL database name |
| `POSTGRES_USER` | PostgreSQL username |
| `POSTGRES_PASSWORD` | PostgreSQL password |
| `DATABASE_URL` | JDBC connection string |
| `MINIO_ROOT_USER` | MinIO admin username |
| `MINIO_ROOT_PASSWORD` | MinIO admin password (min 8 characters) |
| `MINIO_ENDPOINT` | MinIO server URL |
| `MINIO_ACCESS_KEY` | MinIO access key |
| `MINIO_SECRET_KEY` | MinIO secret key |
| `MINIO_BUCKET` | MinIO bucket name |

3. **Build and start**

```bash
docker compose up --build -d
```

4. **Verify**

```bash
docker compose ps          # All services should be "healthy" / "Up"
docker compose logs -f backend   # Watch backend logs
```

The application is now available at `http://localhost:8081`.

### Local Development

**Backend:**

```bash
cd backend
mvn clean package -DskipTests
java -jar target/file_exchange-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

**Frontend:**

```bash
cd frontend
npm ci
npm start    # Dev server on :3000, proxies /api to :8080
```

---

## API Reference

All endpoints are prefixed with `/api/v1`. Protected endpoints require an `Authorization: Bearer <token>` header.

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/users/register` | No | Register a new user |
| `POST` | `/users/login` | No | Authenticate and receive JWT |

**Register** — `POST /api/v1/users/register`

```json
{ "name": "John Doe", "email": "john@example.com", "password": "securePass123" }
```

**Login** — `POST /api/v1/users/login`

```json
{ "email": "john@example.com", "password": "securePass123" }
```

Returns: `{ "token": "eyJhbGciOiJIUzI1NiJ9..." }`

### User Profile

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/users/profile` | Yes | Get current user profile |
| `PUT` | `/users/profile` | Yes | Update name and/or email |

### File Operations

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/files` | Yes | List all files for the authenticated user |
| `POST` | `/files/upload` | Yes | Upload a file (multipart/form-data) |
| `GET` | `/files/download?fileId=N` | Yes | Download a file by ID |
| `DELETE` | `/files?fileId=N` | Yes | Delete a file by ID |
| `POST` | `/files/share?fileId=N` | Yes | Generate a shareable public link |
| `GET` | `/files/public/{token}` | No | Download a file via share token |

---

## Testing

### Unit Tests

The backend has 16 test files covering services, handlers, security, storage, and executor components.

```bash
cd backend
mvn clean test                       # Run all tests
mvn test -Dtest=FileServiceTest      # Run a specific test class
mvn test -Dtest=UserServiceTest
```

**Covered areas:**

- **Services** — User registration/login validation, file CRUD operations, share link generation
- **Handlers** — HTTP request parsing, parameter binding, routing, response conversion
- **Security** — BCrypt password encoding, password validation rules
- **Storage** — MinIO storage operations (mocked)
- **Executor** — Thread pool builder and executor service

### Code Quality

```bash
mvn spotless:check     # Verify code formatting (Palantir Java Format)
mvn spotless:apply     # Auto-fix formatting issues
mvn clean verify       # Full build with JaCoCo coverage enforcement
```

**Coverage thresholds:** 80% line coverage, 60% branch coverage.
JaCoCo excludes: DTOs, entities, annotations, controllers, repositories, `JwtUtil`, `Main`.

### Load Tests (Gatling)

Simulates concurrent users performing the full flow: register → login → upload file.

```bash
cd backend
mvn gatling:test
mvn gatling:test -Dgatling.simulationClass=simulations.FileUploadLoadTest
```

| Parameter | Value |
|---|---|
| Virtual users | 50 |
| Ramp-up period | 30 seconds |
| Success threshold | > 95% |
| p95 response time | < 5000 ms |

### Frontend Tests

```bash
cd frontend
npm test -- --watchAll=false --ci
```

---

## CI/CD

### Continuous Integration (`.github/workflows/ci.yml`)

Triggered on pull requests to `main` and pushes to `main`.

**Backend pipeline:**
1. Setup JDK 22 (Temurin)
2. Code formatting check (`mvn spotless:check`)
3. Build and test with coverage (`mvn clean verify`)
4. Upload JaCoCo coverage report
5. OWASP Dependency Check (fails on CVSS >= 9)

**Frontend pipeline:**
1. Setup Node.js 20
2. Install dependencies (`npm ci`)
3. Run tests
4. Production build (`npm run build`)

### Continuous Deployment (`.github/workflows/deploy.yml`)

Triggered manually via `workflow_dispatch`. Runs the full test suite, builds the frontend, and deploys to the VPS via SCP + SSH with a health check.

---

## Project Structure

```
File-exchanger/
├── backend/
│   ├── src/main/java/com/file_exchange/
│   │   ├── annotations/          # @CustomGetMapping, @CustomPostMapping, etc.
│   │   ├── cli/                  # Main.java entry point
│   │   ├── controllers/          # FileController, UserController
│   │   ├── db/                   # DatabaseConfig (Liquibase init)
│   │   ├── dto/                  # ErrorResponse, FileDto, UserDto
│   │   ├── entity/               # User, File, SharedLink
│   │   ├── exceptions/           # AppException, AuthenticationException, etc.
│   │   ├── executor/             # Thread pool (platform + virtual threads)
│   │   ├── handlers/             # Request pipeline (parser, router, binder, dispatcher)
│   │   ├── http/                 # HttpRequest, HttpResponse models
│   │   ├── repository/           # JDBC repositories (User, File, SharedLink)
│   │   ├── security/             # PasswordEncoder, PasswordValidator
│   │   ├── server/               # CustomWebServer, RouteRegistry, ClientConnectionHandler
│   │   ├── services/             # FileService, UserService
│   │   ├── storage/              # StorageService interface, MinioStorageService
│   │   └── utils/                # JwtUtil
│   ├── src/main/resources/db/migration/
│   │   └── db.changelog-master.xml   # Liquibase migrations (4 changesets)
│   ├── src/test/java/            # 16 test files (JUnit 5 + Mockito)
│   ├── src/test/scala/           # Gatling load tests
│   ├── Dockerfile                # Multi-stage build (Maven → JRE Alpine)
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/           # FileList, Login, Register, Profile, Layout
│   │   ├── pages/                # Route wrappers
│   │   ├── services/api.js       # Axios client with JWT interceptor
│   │   └── styles/               # Custom CSS
│   ├── Dockerfile                # Multi-stage build (Node → Nginx Alpine)
│   └── package.json
├── .github/workflows/
│   ├── ci.yml                    # PR/push: test + lint + OWASP check
│   └── deploy.yml                # Manual: test → build → deploy to VPS
├── docker-compose.yml            # PostgreSQL + MinIO + Backend + Nginx
├── nginx.conf                    # Reverse proxy, gzip, security headers
├── .env.example                  # Environment variable template
└── README.md
```

---

## License

This project is developed as an educational final project.
