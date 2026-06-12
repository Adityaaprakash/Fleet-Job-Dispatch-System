# Fleet Job Dispatch System

## Overview
The Fleet Job Dispatch System is a RESTful API designed to manage logistics operations, driver assignments, and job lifecycle tracking. It solves the business problem of coordinating complex delivery schedules while ensuring data consistency and preventing operational conflicts like double-assignment of drivers.

## Tech Stack
- Java 21
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- H2 (tests)
- Maven
- JUnit 5 + Mockito

## Architecture
The system follows a standard layered architecture comprising Controllers, Services, and Repositories for clear separation of concerns. It implements the DTO pattern to decouple internal database entities from external API contracts, ensuring security and flexibility. The business logic is encapsulated within the service layer, where a specialized state machine governs all job status transitions. Global error handling is centralized in a `GlobalExceptionHandler` to provide consistent JSON error responses. Additionally, the system employs optimistic locking on the Driver entity via the `@Version` annotation to handle concurrent assignment requests safely.

## Database Schema
The database consists of four primary entities: Driver, Vehicle, Job, and JobStatusLog. A Driver represents a person who can be assigned to multiple Jobs over time, while a Vehicle is assigned to a single Driver for operations. Jobs track delivery details and are linked to JobStatusLogs, which provide a complete audit trail of every status change.

Driver ||--o{ Job : "assigned to"
Driver ||--o| Vehicle : "drives"
Job ||--o{ JobStatusLog : "has audit trail"

## Running Locally

### Prerequisites
- Java 21
- Maven 3.8+
- PostgreSQL running on localhost:5432
  - Database: fleet_dispatch
  - Username: postgres / Password: postgres

### Start the app
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run tests
```bash
mvn test
```

## API Endpoints

| Method | Endpoint | Description | Success Code |
| :--- | :--- | :--- | :--- |
| POST | /api/jobs | Create a new job with description and schedule | 201 |
| GET | /api/jobs | Retrieve a list of all jobs in the system | 200 |
| GET | /api/jobs/{id} | Retrieve detailed information for a specific job | 200 |
| PATCH | /api/jobs/{id}/assign | Assign a specific driver to an unassigned job | 200 |
| PATCH | /api/jobs/{id}/status | Update the status of a job (e.g., IN_PROGRESS, COMPLETED) | 200 |
| GET | /api/drivers | Retrieve all drivers (optional filtering by status) | 200 |
| GET | /api/drivers/{id} | Retrieve detailed information for a specific driver | 200 |
| GET | /api/drivers/{id}/jobs | Get the job schedule for a driver on a specific date | 200 |

## Key Design Decisions
1. **DTO Pattern**: Entities are never exposed directly to the API layer; specialized Request/Response DTOs ensure we only expose necessary data and maintain a stable API contract.
2. **Status State Machine**: A robust validation mechanism in `JobService` prevents illegal job transitions (e.g., direct jump from UNASSIGNED to COMPLETED), ensuring operational integrity.
3. **Optimistic Locking**: By using `@Version` on the Driver entity, the system prevents race conditions where two simultaneous requests might try to assign the same driver to different jobs.

## Lessons Learned
* **Trust but Verify with Tests**: During the final verification phase (Day 7), I discovered that while `@Valid` and `@NotNull` annotations were present in the codebase, the system was silently ignoring them. The root cause was a missing `spring-boot-starter-validation` dependency in `pom.xml`. Fixing this revealed hidden payload issues that unit tests alone hadn't caught, emphasizing the importance of controller slice tests and integration smoke tests.

## Error Responses
The API returns a standardized JSON structure for all errors:

```json
{
  "timestamp": "2027-06-01T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "description: must not be blank"
}
```

### Examples:
- **400 Bad Request**: Raised when input validation fails (e.g., missing required fields).
- **404 Not Found**: Raised when a requested Job or Driver ID does not exist.
- **409 Conflict**: Raised when attempting to assign a job to a driver who is already BUSY.
- **422 Unprocessable Entity**: Raised when an illegal status transition is attempted (e.g., moving a COMPLETED job back to ASSIGNED).
