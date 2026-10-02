# SeatLock

SeatLock is a Spring Boot backend for concurrent ticket booking and seat reservations. Phase 1 provides MongoDB-backed CRUD APIs for events, venues, screens, and seats.

## Requirements

- Java 17 or newer
- Maven 3.9+
- MongoDB 6+ (local or hosted)

## Configure MongoDB

The application reads its MongoDB connection string from `MONGODB_URI`. If it is not set, it connects to `mongodb://localhost:27017/seatlock`.

PowerShell:

```powershell
$env:MONGODB_URI = "mongodb+srv://<username>:<password>@<cluster>/<database>"
mvn spring-boot:run
```

Do not commit credentials. Set `MONGODB_URI` in your local environment or deployment secret store.

## Run

```powershell
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.

## API

All resources support `POST` (create), `GET` (list), `GET /{id}`, `PUT /{id}` (replace), and `DELETE /{id}`. Creates return `201 Created`; deletes return `204 No Content`.

| Resource | Endpoint | Notes |
| --- | --- | --- |
| Venues | `/api/venues` | List, create, update, and delete venues |
| Screens | `/api/screens` | Can filter by `?venueId={id}` |
| Seats | `/api/seats` | Can filter by `?screenId={id}`; row/seat positions are unique per screen |
| Events | `/api/events` | Can filter by `?screenId={id}`; end time must be after start time |

Screens require an existing venue. Seats and events require an existing screen. A venue with screens, or a screen with seats or events, cannot be deleted.

Example venue:

```json
{
  "name": "Downtown Cinema",
  "address": "10 Main Street",
  "city": "Seattle"
}
```

Example screen:

```json
{
  "venueId": "<venue-id>",
  "name": "Screen 1",
  "capacity": 120
}
```

Example seat:

```json
{
  "screenId": "<screen-id>",
  "rowLabel": "A",
  "seatNumber": 1,
  "category": "STANDARD"
}
```

Allowed seat categories: `STANDARD`, `PREMIUM`, `ACCESSIBLE`.

Example event (timestamps must be ISO-8601 instants):

```json
{
  "screenId": "<screen-id>",
  "title": "Evening Show",
  "description": "7 PM screening",
  "startsAt": "2026-10-15T19:00:00Z",
  "endsAt": "2026-10-15T21:00:00Z"
}
```

Invalid request fields return `400 Bad Request`, missing resources return `404 Not Found`, and conflicting operations return `409 Conflict`.

## Tests

Run the test suite with:

```powershell
mvn test
```

## Package structure

The code is organized by feature, with a small set of layers within each feature:

```text
com.seatlock
├── booking
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure.persistence
├── event
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure.persistence
├── user
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure.persistence
├── venue
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure.persistence
└── shared
    ├── config
    └── exception
```

Feature-specific code stays together: `api` exposes HTTP endpoints, `application` coordinates use cases, `domain` owns business rules and MongoDB documents, and `infrastructure.persistence` contains repository adapters. Reservation state and concurrency guarantees will be implemented in a later phase; physical seat records are not booking locks.

## Roadmap

- [x] Phase 1: MongoDB configuration and event/venue/screen/seat CRUD APIs
- [ ] Phase 2: Event-specific seat inventory and booking flow
- [ ] Phase 3: Concurrency-safe reservations, expiration, and race-condition tests
- [ ] Phase 4: Redis, RabbitMQ, payment simulation, and idempotency
- [ ] Phase 5: Docker, API documentation, metrics, CI/CD, and deployment
