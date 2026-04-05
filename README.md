# 📡 Repo Radar

A catalog platform for discovering and organizing open-source software projects. The system features a full-stack architecture including a Spring Boot backend with an admin dashboard, a REST API, and a native Android client.

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.x, Spring Data JPA |
| Security | Spring Security (Sessions for Admin, JWT for API) |
| Frontend (Admin) | Thymeleaf + Tailwind CSS |
| Database | MySQL 8.x |
| Mobile | Android Native (Java) |
| Build Tool | Maven |

---

## 🏛 Architecture

The system is divided into three main modules:

1. **Admin Panel (`/admin/**`):** A server-side rendered web app for curators. Allows importing repos via GitHub URL, managing categories and technologies, and toggling project visibility.
2. **REST API (`/api/**`):** Public JSON endpoints for project discovery. Supports stateless JWT authentication for user-specific features like "Favorites".
3. **Android Client:** Native app focused on consumption. Features search, advanced filtering by category and technology, and a personal favorites list.

### Data Model

The core is the `Project` entity. It handles many-to-many relationships with `Category` and `Technology`. Projects remain in `HIDDEN` status until an administrator reviews and switches them to `PUBLISHED`.

### Authentication

The system uses two coexisting authentication mechanisms, each suited to its client:

- **Admin panel** (`/admin/**`): stateful HTTP session via Spring Security.
- **Android app** (`/api/**`): anonymous access by default; optional JWT for registered users (favorites feature).

---

## 🚀 Getting Started

### Prerequisites

- JDK 21
- Maven 3.9+
- MySQL 8.0

### Database Setup

1. Create the schema:

```sql
CREATE DATABASE repo_radar;
```

2. Run the `modelo_de_datos.sql` script located in the `/docs` folder. This creates all tables and constraints.

3. Insert at least one administrator user manually to access the web panel:

```sql
INSERT INTO administrator (name, email, password_hash)
VALUES ('Admin', 'admin@reporadar.com', '<bcrypt_hash>');
```

> **Note:** The password must be stored as a BCrypt hash. Spring Security will not accept plain text.

### Configuration

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/repo_radar
spring.datasource.username=your_user
spring.datasource.password=your_password

# Schema must exist before startup (created by modelo_de_datos.sql)
spring.jpa.hibernate.ddl-auto=validate
```

### Run

```bash
./mvnw spring-boot:run
```

The server will be available at `http://localhost:8080`.

---

## 📈 Roadmap

- [x] Database schema & JPA entity mapping
- [x] Repository layer (Spring Data JPA)
- [ ] GitHub API integration (`GitHubImportService`)
- [ ] Spring Security: admin session + JWT for API
- [ ] Admin UI: project management, categories & technologies
- [ ] REST API: public endpoints & JWT integration
- [ ] Android App: main feed, search & filters
- [ ] Android App: user registration, login & favorites

---

## 📂 Project Structure

```
repo-radar/
├── src/main/java/com/reporadar/
│   ├── entity/          # JPA Models
│   ├── repository/      # Query layer
│   ├── service/         # Business logic
│   ├── controller/      # Admin (Thymeleaf) & API (REST)
│   ├── security/        # Auth config (Session + JWT)
│   └── dto/             # Data Transfer Objects
└── src/main/resources/
    ├── templates/       # Thymeleaf Views
    └── static/          # CSS/JS Assets
```

---

## 📄 License

This project is licensed under the MIT License.
