# Calorie Tracker

A web-based calorie and nutrition tracking application built for EECS 447 (Database Systems) at the University of Kansas. Users can register, log meals, track food intake, set daily calorie/macro goals, and view nutrition summaries.

## Tech Stack

- **Backend:** Java 17 + Spring Boot 3.x + Spring JDBC (raw SQL)
- **Frontend:** React.js
- **Database:** PostgreSQL 16
- **Auth:** HTTP Sessions via Spring Session

## Prerequisites

- [Java 17+](https://adoptium.net/)
- [Node.js 18+](https://nodejs.org/)
- [Docker + Docker Compose](https://docs.docker.com/get-docker/)

## Running Locally

### 1. Start the Database

```bash
docker-compose up -d
```

This starts a PostgreSQL 16 container and runs `database/init.sql` to create the schema and seed data.

### 2. Start the Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API server starts on `http://localhost:8080`.

### 3. Start the Frontend

```bash
cd frontend
npm install
npm start
```

The app opens at **http://localhost:3000**.

## Default URLs

| Service  | URL                      |
|----------|--------------------------|
| Frontend | http://localhost:3000    |
| Backend  | http://localhost:8080    |
| Database | localhost:5432 (postgres)|

## Project Structure

```
calorie-tracker/
├── backend/          # Spring Boot REST API
├── frontend/         # React app
├── database/
│   └── init.sql      # Schema and seed data
└── docker-compose.yml
```

## Stopping the App

```bash
# Stop the database container
docker-compose down
```
