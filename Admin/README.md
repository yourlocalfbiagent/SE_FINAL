# Admin module

Java 17 + Spring Boot 3.3 + PostgreSQL + Flyway, with a REST API and a tiny single-page test UI.

## Run

Database creds default to `ahmad` / `1234` against `localhost:5432/se_final_admin`. Override with env vars `DB_URL`, `DB_USER`, `DB_PASSWORD` if needed.

```bash
cd Admin
mvn spring-boot:run
```

Spring Boot does the rest: applies Flyway migrations, opens an embedded Tomcat on `:8080`, seeds a demo tenant on first run.

## Access

- **Test GUI:** http://localhost:8080/  (single-page, lets you exercise every endpoint)
- **REST API:** under `/api`

### Endpoints
| Method | Path                                              | What                                       |
|--------|---------------------------------------------------|--------------------------------------------|
| GET    | `/api/companies`                                  | List all companies                         |
| POST   | `/api/companies`                                  | Create a company                           |
| GET    | `/api/companies/{id}`                             | Get one company                            |
| GET    | `/api/companies/{id}/roles`                       | List roles for a company                   |
| POST   | `/api/companies/{id}/roles`                       | Create a role inside a company             |
| GET    | `/api/companies/{id}/users`                       | List users in a company                    |
| POST   | `/api/companies/{id}/users`                       | Create a user (password BCrypt-hashed)     |
| GET    | `/api/roles/{roleId}`                             | Get one role                               |
| GET    | `/api/roles/{roleId}/permissions`                 | List permissions granted to a role         |
| POST   | `/api/roles/{roleId}/permissions/{permissionId}`  | Grant a permission                         |
| DELETE | `/api/roles/{roleId}/permissions/{permissionId}`  | Revoke a permission                        |
| GET    | `/api/users/{id}`                                 | Get one user                               |
| GET    | `/api/users?email=...`                            | Look up a user by email                    |
| GET    | `/api/modules`                                    | Catalog: modules                           |
| GET    | `/api/actions`                                    | Catalog: actions                           |
| GET    | `/api/permissions`                                | Catalog: every (module, action) permission |

### Layout
- `src/main/resources/db/migration/` — Flyway migrations
- `src/main/resources/static/index.html` — the test GUI
- `src/main/java/.../web/` — REST controllers + error handlers + DTOs
- `src/main/java/.../dao/` — JDBC DAOs (one per table)
- `src/main/java/.../model/` — record models
- `src/main/java/.../config/BeanConfig.java` — wires DAOs and the BCrypt encoder
- `src/main/java/.../bootstrap/DataSeeder.java` — on-first-run demo tenant
