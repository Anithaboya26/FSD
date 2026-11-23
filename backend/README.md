# Ventixe API

Spring Boot REST API for VENTIXE. It exposes public event discovery, JWT-backed customer/organizer authentication, and organizer event management. Booking, payment, and administrator workflows are not implemented yet.

## Requirements

- Java 21
- Maven 3.9+
- MySQL 8.0.16+

## Database setup

1. Create a local MySQL database and application user using your own local credentials.
2. Apply [`../database/schema.sql`](../database/schema.sql) to the database.
3. Set the database and JWT settings in your shell or IDE run configuration. Never commit credentials. Generate a fresh signing key in PowerShell:

   ```powershell
   $env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
   $env:DB_USERNAME = "your_local_db_user"
   $env:DB_PASSWORD = "your_local_db_password"
   ```

4. On HTTPS deployments set `AUTH_COOKIE_SECURE=true` and set `CORS_ALLOWED_ORIGINS` to the exact deployed frontend origins. Never reuse a committed or public signing key.

The application uses `spring.jpa.hibernate.ddl-auto=validate`: it checks the existing schema and never creates, alters, or drops production tables.

## Run and test

From this directory, after setting `JWT_SECRET` and your local database environment:

```powershell
mvn spring-boot:run
mvn test
```

By default the API listens on port 8080. OpenAPI documentation is available at `/swagger-ui.html`; readiness and liveness checks are exposed by Actuator under `/actuator/health`.

## Public endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/categories` | List active event categories |
| GET | `/api/v1/events` | Search published events; supports `q`, `city`, `category`, `from`, `to`, `page`, `size`, and `sort` |
| GET | `/api/v1/events/{slug}` | Read a published event and its ticket options |
| GET | `/api/v1/auth/csrf` | Issue the CSRF cookie/token pair required before state-changing requests |
| POST | `/api/v1/auth/register` | Register a customer or organizer; organizer registration also requires `businessName` |
| POST | `/api/v1/auth/login` | Authenticate and set an HttpOnly JWT cookie |
| GET | `/api/v1/auth/me` | Return the currently authenticated account |
| POST | `/api/v1/auth/logout` | Expire the auth cookie |

`sort` supports `soonest` (default), `latest`, and `name`. Page size is limited to 50. Dates use ISO `YYYY-MM-DD`; the end date is inclusive. Event times are local to the event's returned `timezone`.

Ticket availability in catalog responses is informational. Booking must recheck and reserve inventory transactionally; a displayed quantity is not a reservation.

## Organizer event endpoints

All endpoints below require an authenticated organizer account and the CSRF header on state-changing requests.

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/organizer/events` | List the authenticated organizer's events |
| POST | `/api/v1/organizer/events` | Create an event draft with venue and ticket types |
| GET | `/api/v1/organizer/events/{eventId}` | Read an owned event and its active ticket types |
| PUT | `/api/v1/organizer/events/{eventId}` | Update an owned draft or rejected event |
| POST | `/api/v1/organizer/events/{eventId}/submit` | Submit a draft or rejected event for approval |
| DELETE | `/api/v1/organizer/events/{eventId}` | Archive an eligible owned event; does not delete its records |

Creation saves events as `DRAFT`; submission changes the state to `PENDING_APPROVAL`. Only draft and rejected events can be edited. Changes that would alter inventory are blocked once tickets are reserved or sold. Event image upload and administrator approval are deferred to later phases.

## Authentication notes

- Passwords are BCrypt-hashed; registration requires at least 12 characters and rejects passwords longer than 72 UTF-8 bytes.
- Self-registration only allows `CUSTOMER` and `ORGANIZER`; organizer accounts start with pending organizer verification. Administrators must be provisioned by a controlled operator process, never by public registration.
- JWTs are signed with the Base64 `JWT_SECRET`, expire after 15 minutes by default, and are stored only in an HttpOnly cookie. The API reloads the account and role for each authenticated request so suspended accounts lose access immediately.
- The frontend must send `withCredentials` and the `X-XSRF-TOKEN` header obtained from `/api/v1/auth/csrf` on POST/PUT/PATCH/DELETE requests. CSRF protection remains enabled.
- CORS allows only the configured exact origins and credentials. Use HTTPS and `AUTH_COOKIE_SECURE=true` in production.
- Role-scoped route families are enforced server-side: `/api/v1/customer/**`, `/api/v1/organizer/**`, and `/api/v1/admin/**`. A frontend route guard is not an authorization boundary.
- Email verification and password recovery are future integration phases; registration currently activates the account for the demonstrable auth workflow.
