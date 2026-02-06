# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

File-Exchanger is a full-stack file sharing application with a **custom Java web server** backend (not Spring Boot) and a **React** frontend. Users register, log in, and upload/download/delete files with JWT-based authentication.

## Build & Run Commands

### Backend (Java 22, Maven)
```bash
cd backend
mvn clean package -DskipTests          # Build fat JAR
mvn clean test                          # Run unit tests
java -jar target/file_exchange-0.0.1-SNAPSHOT-jar-with-dependencies.jar  # Run server on port 8080
```

### Frontend (React 19, npm)
```bash
cd frontend
npm ci                                  # Install dependencies
npm start                               # Dev server (proxies API to localhost:8080)
npm run build                           # Production build
```

### Docker
```bash
docker-compose up --build -d            # Start backend + nginx
docker-compose down                     # Stop services
```
Note: Frontend must be built before Docker deployment (nginx serves static files from `frontend/build/`).

### Load Tests (Gatling)
```bash
cd backend
mvn gatling:test                        # Run load tests (in src/test/scala/)
```

## Architecture

### Backend — Custom HTTP Server with Reflection-Based Routing

The backend is a **from-scratch HTTP server** using Java `ServerSocket`, not Spring Boot. Key architectural concepts:

- **`CustomWebServer`** (`server/`) — Listens on port 8080, manages a thread pool (supports virtual threads), and uses reflection to discover and register controller methods at startup.
- **Custom annotations** (`annotations/`) — Mirror Spring's annotations: `@CustomRestController`, `@CustomGetMapping`, `@CustomPostMapping`, `@CustomRequestBody`, `@CustomPathVariable`, `@CustomRequestParam`, `@CustomRequestHeader`, `@CustomRequestPart`, etc.
- **Request dispatch pipeline** (`handlers/dispatcher/`) — `RequestDispatcher` routes requests → `ParameterBinder` maps request data to method params → `ResponseConverter` serializes return values to HTTP responses. `HttpRequestParser` handles raw HTTP parsing including multipart form data.
- **Controllers** (`controllers/`) — `FileController` and `UserController` define REST endpoints.
- **Services** (`services/`) — `FileService` and `UserService` contain business logic. File sanitization prevents path traversal.
- **Repositories** (`repository/`) — Direct JDBC to SQLite. No ORM.
- **Database** — SQLite (`users.db` at project root). Schema initialized by `DatabaseInitializer`. Tables: `users` and `files` with FK relationship.
- **Auth** — JWT via JJWT library (`JwtUtil`). Token passed in `Authorization` header.
- **File storage** — Files stored in `uploads/{userId}/{fileName}` on disk.

### API Endpoints
```
POST   /api/v1/users/register     # Body: {name, email, password}
POST   /api/v1/users/login        # Body: {email, password} → returns JWT
GET    /api/v1/files               # List user's files (requires JWT)
POST   /api/v1/files/upload        # Multipart file upload (requires JWT)
GET    /api/v1/files/download?fileId=N  # Download file (requires JWT)
DELETE /api/v1/files?fileId=N      # Delete file (requires JWT)
```

### Frontend

React SPA with React Router. Key structure:
- `services/api.js` — Axios client with JWT interceptor (token from localStorage)
- `components/` — `Login`, `Register`, `FileList`, `Layout`
- `pages/` — Route wrappers for login, register, files
- Routes: `/login`, `/register`, `/files` (protected), `/` redirects to `/files`

### Infrastructure
- **Nginx** — Reverse proxy (nginx.conf). Serves frontend static files, proxies `/api/` to backend. Max upload: 200MB.
- **Docker Compose** — `backend` service (Java JAR) + `nginx` service. Backend volumes: `users.db` and `uploads/`.
- **CI** (`.github/workflows/ci.yml`) — PR to main triggers: Maven tests + frontend build.
- **Deploy** (`.github/workflows/deploy.yml`) — Manual trigger. Builds frontend, SCPs to VPS, runs docker-compose.

## Environment

Requires a `.env` file (see `.env.example`):
- `JWT_SECRET` — Required, min 32 chars for HS256 signing.

## Testing

Unit tests are in `backend/src/test/java/com/file_exchange/unit/services/` using JUnit 5, Mockito, and AssertJ. Run a single test class:
```bash
cd backend
mvn test -Dtest=FileServiceTest
mvn test -Dtest=UserServiceTest
```
