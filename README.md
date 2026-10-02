# SeatLock

SeatLock is a Spring Boot backend for concurrent ticket booking and seat reservations.

## Requirements

- Java 17 or newer
- Maven 3.9+

## Run

```powershell
mvn spring-boot:run
```

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

Feature-specific code stays together: `api` exposes HTTP endpoints, `application` coordinates use cases, `domain` owns business rules, and `infrastructure.persistence` contains database adapters. Database, Redis, and messaging dependencies will be introduced in the steps that implement those integrations.
