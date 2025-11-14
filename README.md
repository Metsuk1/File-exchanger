# File Exchange Platform

**File-Exchanger** is a modern, secure, and fast file-sharing web application built with **Java (backend)**, **React (frontend)**, **Nginx**, and **Docker**.  
It allows users to **upload, download, and manage files** with **JWT-based authentication**, **SQLite database**, and **production-ready architecture**.

Live demo: http://85.198.88.69

---

## Features

- User **registration & login** (JWT tokens)
- Secure **file upload/download** (with size limit 200MB)
- Persistent storage using **SQLite**
- **Nginx reverse proxy** with gzip, caching, and static file serving
- Fully **containerized** with Docker (2 containers: backend + nginx)
- Production-ready: **SSL-ready**, **scalable**, **low RAM usage**
- Clean, responsive **React frontend**

---

## Tech Stack

| Layer        | Technology                          |
|-------------|-------------------------------------|
| Backend     | Java 22 + Custom HTTP Server        |
| Frontend    | React + Vite + Tailwind CSS         |
| Database    | SQLite (`users.db`)                 |
| Web Server  | Nginx (Alpine)                      |
| Auth        | JWT (HMAC-SHA256)                   |
| Container   | Docker + Docker Compose             |
| Deployment  | VPS (Ubuntu), SSL via Let's Encrypt |

---

## Prerequisites

- Docker & Docker Compose
- Git
- Node.js 20+ (for local frontend dev)
- Java 22 (for local backend dev)

---
# Testing

Unit and Integration Tests (JUnit 5 + Mockito)

## User registration:


- Successful registration with valid data


- Validation failures: empty name, null or invalid email format


## User login:


- Successful login with correct credentials


- User not found error


- Incorrect password error


- JWT token generation is mocked and verified


## File operations (integration on embedded custom server):

- Successful upload and download of a small text file

- Large file upload (≈10 MB)

- Unauthorized access rejection when Authorization header is missing

- 404 error when downloading a non-existent file

## How to run:

Maven: mvn test

IDE: run specific test classes from the test panel

# Load Tests (Gatling)


## Scenario flow:

- Register → Login → Upload file (multipart/form-data)


## Dynamic data:

- Unique users per virtual user via a feeder (randomized email/name)

## File upload:

- Real file sent as multipart part with name="file", filename, and proper Content-Type


## Load profile:

- Ramp-up of 50 users over 30 seconds (baseline profile)


## Suggested pass/fail criteria (assertions):

- Successful requests > 95%


- p95 response time < target SLO (e.g., 3000–5000 ms for file operations)


## How to run:

- Place test file at: src/test/resources/bodies/test-file.txt


- Run all simulations: mvn gatling:test


- Run a specific simulation: mvn gatling:test -Dgatling.simulationClass=simulations.FileUploadLoadTest


## What is validated:

- Authentication and JWT acquisition work under load


- Multipart formation is correct and recognized by the server (name="file", filename present)


- API stability under concurrent registrations/logins/uploads


- No 5xx errors with valid requests at baseline load
