# File Exchange Platform

**File-Exchanger** is a modern, secure, and fast file-sharing web application built with **Java (backend)**, **React (frontend)**, **Nginx**, and **Docker**.  
It allows users to **upload, download, and manage files** with **JWT-based authentication**, **SQLite database**, and **production-ready architecture**.

Live demo: http://85.198.88.69/login

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
