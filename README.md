# Personal Finance Manager API

Backend REST API for tracking personal income, expenses, custom categories, savings goals, and generating financial reports.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.4.1
- **Security:** Spring Security (Session-based authentication)
- **Database:** PostgreSQL (supports Supabase / local instance)
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven


## Requirements

- JDK 21 or higher
- Maven 3.9+
- PostgreSQL database instance

## Local Setup

### 1. Repository Setup

```bash
git clone https://github.com/sarthak-jain03/Finance-Manager
cd Syfe_Assignment_Sarthak_Jain
```

### 2. Database Configuration

Database details are configured in `src/main/resources/application.properties`. Ensure your PostgreSQL server details match:

```properties
spring.datasource.url=jdbc:postgresql://<host>:5432/<dbname>
spring.datasource.username=<username>
spring.datasource.password=<password>
spring.jpa.hibernate.ddl-auto=update
```

### 3. Run Application

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

The server runs on `http://localhost:8080`.



## Key Features & API Endpoints

### Authentication (`/api/auth`)
- `POST /api/auth/register` — Register a new account
- `POST /api/auth/login` — Login and establish session cookie
- `POST /api/auth/logout` — Invalidate user session

### Transactions (`/api/transactions`)
- `POST /api/transactions` — Add income or expense transaction
- `GET /api/transactions` — Filter transactions (`startDate`, `endDate`, `categoryId`, `type`)
- `GET /api/transactions/{id}` — Get transaction details
- `PUT /api/transactions/{id}` — Update description, amount, or category
- `DELETE /api/transactions/{id}` — Remove transaction

### Categories (`/api/categories`)
- `GET /api/categories` — View default and custom categories
- `POST /api/categories` — Create custom category
- `DELETE /api/categories/{id}` — Remove custom category

### Savings Goals (`/api/savings-goals`)
- `POST /api/savings-goals` — Set savings goal with target date and amount
- `GET /api/savings-goals` — List goals with calculated progress
- `GET /api/savings-goals/{id}` — Fetch goal status
- `PUT /api/savings-goals/{id}` — Update target amount or target date
- `DELETE /api/savings-goals/{id}` — Delete savings goal

### Financial Reports (`/api/reports`)
- `GET /api/reports/monthly?month=9&year=2026` — Get monthly summary (total income, total expense, category breakdown)
- `GET /api/reports/yearly?year=2026` — Get yearly financial overview

---


## Architectural & Design Notes

1. **Layered Structure:** Standard Controller-Service-Repository pattern. Services handle business logic while Controllers handle request/response mapping and validation.
2. **Session Security:** Spring Security manages HTTP sessions with HTTP-only cookies. Passwords are hashed using BCrypt.
3. **Data Isolation:** All data queries bind explicitly to the authenticated user retrieved from `SecurityContext`.
4. **Calculated Progress:** Goal progress is evaluated dynamically from transactions rather than stored as redundant state.
5. **Default Seeding:** Built-in default categories are auto-seeded on startup via `DataInitializer`.

---

## Error Handling

Standardized response format across all API exceptions:

```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-09-05T00:10:00",
  "errors": {
    "amount": "Amount must be greater than zero"
  }
}
```

---

## Folder Structure

```
src/main/java/com/sarthak/finance/
├── config/              # App initialization & default seeding
├── controller/          # REST endpoints
├── dto/                 # Request & response models
├── exception/           # Custom exceptions & global exception handler
├── model/               # JPA Entity models
├── repository/          # Spring Data JPA repositories
├── security/            # Security configs, user details service
└── service/             # Business logic services
```
