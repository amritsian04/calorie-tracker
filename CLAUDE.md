# Calorie Tracking System

## Project Overview
A web-based calorie and nutrition tracking application for EECS 647 (Database Systems) at the University of Kansas. Users can register, log meals, track food intake, set daily calorie/macro goals, and view nutrition summaries.

## Tech Stack
- **Backend:** Java 17+ with Spring Boot 3.x, Spring JDBC (JdbcTemplate) for raw SQL queries
- **Frontend:** React.js
- **Database:** PostgreSQL 16
- **Auth:** HTTP Sessions via Spring Session (required for 3-person team)
- **Containerization:** Docker Compose for PostgreSQL

## Important Constraints
- This is a DATABASE CLASS project. The professor grades SQL queries directly.
- Use JdbcTemplate with raw SQL — do NOT use JPA, Hibernate, or any ORM.
- Every query should be visible as a SQL string in the Java code.
- Do NOT use any open-source or third-party code beyond the SDK/framework itself.

## Database Schema
See `database/init.sql` for the full schema. Five tables:
- `users` — user accounts
- `meal` — meals logged by users (breakfast, lunch, dinner, snack)
- `food` — food catalog with nutrition info
- `meal_entry` — junction table linking meals to foods with quantity (resolves M:N)
- `daily_goal` — per-date calorie and macro goals

## Required Features (Rubric)
1. **3+ tables** — we have 5
2. **5+ dynamic queries** (queries with user input in predicates):
   - Search foods by name (ILIKE)
   - View daily calorie summary (JOIN meal + meal_entry + food, filter by user + date)
   - View weekly summary (JOIN + GROUP BY over date range)
   - Goal vs actual comparison (JOIN meal + meal_entry + food + daily_goal)
   - View all meals for a user (JOIN meal + meal_entry + food, filter by user)
   - Search foods by category
3. **2+ queries with JOIN** — covered by items above
4. **Database updates** — INSERT/UPDATE/DELETE for meals, meal entries, goals
5. **Sessions** — HTTP session for login/logout, session stores user_id

## Project Structure
```
calorie-tracker/
├── backend/          # Spring Boot app
│   └── src/main/
│       ├── java/com/calorietracker/
│       │   ├── controller/    # REST controllers
│       │   ├── repository/    # JdbcTemplate DAOs with raw SQL
│       │   ├── model/         # POJOs
│       │   └── config/        # Session config, CORS
│       └── resources/
│           └── application.properties
├── frontend/         # React app
├── database/
│   └── init.sql      # Schema + seed data
└── docker-compose.yml
```

## Running Locally
1. Start PostgreSQL: `docker-compose up -d`
2. Start backend: `cd backend && ./mvnw spring-boot:run`
3. Start frontend: `cd frontend && npm start`

## Code Style
- Clean, well-commented code
- Descriptive variable names
- SQL queries as multi-line strings for readability
- REST endpoints follow standard conventions (GET, POST, PUT, DELETE)
