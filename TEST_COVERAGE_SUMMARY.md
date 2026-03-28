# Test Coverage Summary

This document outlines all test categories and their coverage.

---

## Test Overview

| Folder | Test Class | Type | Count | Purpose |
|--------|------------|------|-------|---------|
| unit/ | PersonRepositoryTest.kt | Unit | 14 | Test data layer with in-memory repository |
| unit/ | PersonServiceTest.kt | Unit | 23 | Test business logic with mocked repository |
| template/ | HandlebarsTest.kt | Unit | 22 | Test Handlebars template rendering |
| security/ | RateLimiterTest.kt | Unit | 9 | Test rate limiting functionality |
| security/ | SecurityTest.kt | Unit | 15 | Test sanitization and security helpers |
| integration/ | WebServerIntegrationTest.kt | Integration | 10 | Test full HTTP endpoints with database |
| | **Total** | | **93** | |

---

## 1. PersonRepositoryTest.kt (14 Tests)

**Purpose:** Unit tests for the data access layer using an in-memory implementation.

### Test Coverage

| Category | Tests | Description |
|----------|-------|-------------|
| **Save Operations** | 3 | Inserting persons and returning with generated ID |
| **Find By ID** | 3 | Retrieving single persons by ID |
| **Find All** | 3 | Retrieving all persons |
| **Update Operations** | 3 | Modifying existing persons |
| **Delete Operations** | 2 | Removing persons |

### Specific Tests

```
save inserts person and returns with id
save generates unique ids
save returns person with createdAt timestamp
findById returns person when exists
findById returns person with createdAt
findById returns null when not exists
findAll returns all persons
findAll returns all persons with createdAt
findAll returns empty list when no persons
update modifies existing person
update preserves original createdAt
update returns null when person not exists
delete removes person and returns true
delete returns false when person not exists
```

### Implementation

Uses `InMemoryPersonRepository` - a mock implementation of `PersonRepositoryInterface` that stores data in a `MutableMap`.

---

## 2. PersonServiceTest.kt (23 Tests)

**Purpose:** Unit tests for business logic layer using a mock repository.

### Test Coverage

| Category | Tests | Description |
|----------|-------|-------------|
| **Create - Valid** | 2 | Creating persons with valid data |
| **Create - Name Validation** | 2 | Rejecting blank/empty names |
| **Create - Age Validation** | 3 | Rejecting negative ages, max age, accepting zero |
| **Create - Profession Validation** | 2 | Rejecting blank/whitespace professions |
| **Create - City Validation** | 2 | Rejecting blank/whitespace cities |
| **Create - Whitespace Trimming** | 1 | Trimming input whitespace |
| **Read Operations** | 2 | Getting single person and all persons |
| **Update Operations** | 2 | Updating persons with valid/invalid data |
| **Update - Validation** | 4 | Validating inputs during update |
| **Delete Operations** | 2 | Deleting persons with exists/not-exists |

### Specific Tests

```
createPerson saves valid person
createPerson throws when name is blank
createPerson throws when name is empty
createPerson throws when age is negative
createPerson accepts age of zero
createPerson throws when age exceeds maximum
createPerson throws when profession is blank
createPerson throws when profession is whitespace
createPerson throws when city is blank
createPerson throws when city is whitespace
createPerson trims whitespace
getPerson returns person when exists
getPerson returns null when not exists
getAllPeople returns all persons
getAllPeople returns empty list when no persons
updatePerson updates when valid
updatePerson returns null when person not exists
updatePerson throws when name is blank
updatePerson throws when age is negative
updatePerson throws when profession is blank
updatePerson throws when city is blank
deletePerson returns true when person exists
deletePerson returns false when person not exists
```

### Validation Rules Tested

- Name cannot be blank or empty
- Age must be >= 0
- Profession cannot be blank
- City cannot be blank
- All string inputs are trimmed

### Implementation

Uses `MockPersonRepository` - a configurable mock that allows setting expected return values and tracking saved data.

---

## 3. HandlebarsTest.kt (22 Tests)

**Purpose:** Unit tests for Handlebars template engine, custom helpers, and dynamic rendering.

### Test Coverage

| Category | Tests | Description |
|----------|-------|-------------|
| **FormatDate Helper** | 4 | Date formatting with nullable support |
| **HTML Escape Helper** | 1 | HTML escaping for display |
| **JS Escape Helper** | 3 | JavaScript escaping for inline handlers |
| **Template Loading** | 1 | Loading templates from classpath |
| **HTML Structure** | 1 | Verifying HTML renders correctly |
| **People Rendering** | 1 | Server-side rendering with data |
| **Form Elements** | 1 | Verifying all form fields present |
| **Action Buttons** | 1 | Verifying Edit/Delete buttons |
| **Table Headers** | 1 | Verifying table with sort links |
| **Search/Filter** | 2 | Search form and filtered count |
| **Empty States** | 2 | Empty list and search no results |
| **Count Display** | 2 | Total and filtered count display |
| **JavaScript/API** | 1 | Verifying API call handlers |
| **Each Helper** | 1 | Iterating over people list |

### Specific Tests

```
formatDate helper converts LocalDateTime to date
formatDate helper converts ISO datetime string to date
formatDate helper handles invalid datetime
formatDate helper handles non-string input
jsEscape helper escapes quotes and prevents XSS
jsEscape helper escapes double quotes
jsEscape helper escapes newlines
htmlEscape helper escapes special characters
template loads from classpath
template renders HTML structure
template renders people with server-side data
template renders all required form fields
template includes Edit and Delete buttons
template includes table headers with sort links
template links to external stylesheet
template includes search form
template shows empty state when no people
template shows filtered message when searching
template shows count when not filtered
template shows filtered count when filtered
template includes JavaScript for form handling
each helper renders multiple people
```

### Custom Helpers Tested

- `formatDate` - Converts LocalDateTime or ISO datetime strings to date format (handles nullable)
- `htmlEscape` - HTML escaping for displaying user content
- `jsEscape` - JavaScript escaping for inline event handlers

---

## 4. WebServerIntegrationTest.kt (10 Tests)

**Purpose:** Integration tests for full HTTP endpoints with real PostgreSQL database.

### Test Coverage

| Category | Tests | Description |
|----------|-------|-------------|
| **GET /api/people** | 1 | Retrieving all persons |
| **POST /api/people** | 3 | Creating new persons |
| **GET /api/people/:id** | 2 | Retrieving single person |
| **PUT /api/people/:id** | 2 | Updating persons |
| **DELETE /api/people/:id** | 2 | Deleting persons |

### Specific Tests

```
GET api people returns seeded data
POST api people creates new person
POST api people returns error for blank name
POST api people returns error for negative age
GET api people id returns person when exists
GET api people id returns error when not exists
PUT api people updates person
PUT api people returns error when not exists
DELETE api people removes person
DELETE api people returns error when not exists
```

### Setup

- Starts actual Spark server on port 4567
- Connects to real PostgreSQL database
- Uses test-specific database configuration
- Cleans up database after each test
- Uses Handlebars for template rendering

### Requirements

- PostgreSQL must be running
- Database `persondb` must exist
- User credentials must be configured

---

## 5. RateLimiterTest.kt (9 Tests)

**Purpose:** Unit tests for rate limiting functionality.

### Test Coverage

| Category | Tests | Description |
|----------|-------|-------------|
| **Allow/Deny** | 3 | First request, within limit, exceeded |
| **IP Isolation** | 1 | Different IPs have separate limits |
| **Remaining Count** | 2 | Correct remaining count calculation |
| **Reset Time** | 2 | Reset time calculation |
| **Thread Safety** | 1 | Concurrent requests handling |

### Specific Tests

```
isAllowed returns true for first request
isAllowed returns true within limit
isAllowed returns false when limit exceeded
different IPs have separate limits
getRemaining returns correct count
getRemaining returns max for new IP
getResetTime returns positive for existing IP
getResetTime returns zero for new IP
thread safety - concurrent requests
```

---

## 6. SecurityTest.kt (15 Tests)

**Purpose:** Unit tests for security sanitization and helpers.

### Test Coverage

| Category | Tests | Description |
|----------|-------|-------------|
| **Search Sanitization** | 4 | Dangerous chars, length, trimming, valid input |
| **Sort Validation** | 4 | Valid columns, invalid fallback, case, SQL injection |
| **HTML Escape** | 2 | Character escaping, empty string |
| **JS Escape** | 2 | Quote escaping, empty string |
| **Constants** | 2 | MAX_AGE and MAX_NAME_LENGTH validation |
| **XSS Prevention** | 1 | Multiple XSS payloads blocked |

### Specific Tests

```
sanitizeSearchQuery removes dangerous characters
sanitizeSearchQuery limits length to 100
sanitizeSearchQuery trims whitespace
sanitizeSearchQuery preserves valid input
sanitizeSortParam accepts valid columns
sanitizeSortParam returns id for invalid input
sanitizeSortParam is case insensitive
sanitizeSortParam handles SQL injection attempt
htmlEscape escapes all dangerous characters
htmlEscape handles empty string
jsEscape escapes quotes and special chars
jsEscape handles empty string
MAX_AGE validation works
MAX_NAME_LENGTH validation works
search query sanitization prevents XSS
```

---

## Test Architecture

```
src/test/kotlin/com/example/
├── unit/                        # Unit tests (no external dependencies)
│   ├── PersonRepositoryTest.kt  # Repository tests with in-memory storage
│   └── PersonServiceTest.kt     # Service tests with mock repository
│
├── integration/                 # Integration tests (requires database)
│   └── WebServerIntegrationTest.kt  # Full HTTP endpoint tests
│
├── security/                    # Security-focused unit tests
│   ├── RateLimiterTest.kt      # Rate limiting tests
│   └── SecurityTest.kt          # XSS/sanitization tests
│
└── template/                    # Template engine unit tests
    └── HandlebarsTest.kt       # Handlebars helper & rendering tests
```

```
┌─────────────────────────────────────────────────────────────┐
│                     WebServer.kt                             │
│                    (Spark Server)                            │
│              - REST API endpoints                           │
│              - Server-side template rendering               │
│              - Search and sort filtering                     │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│           integration/WebServerIntegrationTest.kt           │
│                   (Integration Tests)                       │
│              Tests full HTTP endpoints                     │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    PersonService.kt                          │
│                  (Business Logic)                           │
│              - Validation                                   │
│              - CRUD operations                             │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  PersonRepository.kt                         │
│                   (Data Access)                             │
│              - JDBC operations                              │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 unit/PersonServiceTest.kt                   │
│         (Unit Tests - Mock Repository)                      │
│              - Business logic validation                    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              unit/PersonRepositoryTest.kt                   │
│          (Unit Tests - In-Memory Repository)               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   template/HandlebarsTest.kt                │
│              (Unit Tests - Template Engine)                 │
│              - Template rendering                          │
│              - Custom helpers                             │
│              - Dynamic content                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   security/RateLimiterTest.kt               │
│              (Unit Tests - Rate Limiting)                  │
│              - IP-based rate limiting                     │
│              - Thread safety                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    security/SecurityTest.kt                 │
│              (Unit Tests - Security)                       │
│              - Input sanitization                         │
│              - XSS prevention                             │
│              - Validation helpers                         │
└─────────────────────────────────────────────────────────────┘
```
┌─────────────────────────────────────────────────────────────┐
│                     WebServer.kt                             │
│                    (Spark Server)                            │
│              - REST API endpoints                           │
│              - Server-side template rendering               │
│              - Search and sort filtering                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  WebServerIntegrationTest.kt                 │
│                   (Integration Tests)                       │
│              Tests full HTTP endpoints                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    PersonService.kt                          │
│                  (Business Logic)                           │
│              - Validation                                   │
│              - CRUD operations                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  PersonRepository.kt                        │
│                   (Data Access)                             │
│              - JDBC operations                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 PersonServiceTest.kt                         │
│         (Unit Tests - Mock Repository)                      │
│              - Business logic validation                    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              PersonRepositoryTest.kt                        │
│          (Unit Tests - In-Memory Repository)               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   HandlebarsTest.kt                         │
│              (Unit Tests - Template Engine)                │
│              - Template rendering                          │
│              - Custom helpers                             │
│              - Dynamic content                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   RateLimiterTest.kt                        │
│              (Unit Tests - Rate Limiting)                  │
│              - IP-based rate limiting                     │
│              - Thread safety                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    SecurityTest.kt                          │
│              (Unit Tests - Security)                       │
│              - Input sanitization                         │
│              - XSS prevention                             │
│              - Validation helpers                         │
└─────────────────────────────────────────────────────────────┘
```

---

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests "com.example.unit.PersonServiceTest"
./gradlew test --tests "com.example.unit.PersonRepositoryTest"
./gradlew test --tests "com.example.template.HandlebarsTest"
./gradlew test --tests "com.example.integration.WebServerIntegrationTest"
```

### Run Unit Tests Only (No Database Required)
```bash
./gradlew test --tests "com.example.unit.*"
./gradlew test --tests "com.example.template.*"
./gradlew test --tests "com.example.security.*"
```

### Run Integration Tests (Requires Database)
```bash
./gradlew test --tests "com.example.integration.*"
```

---

## Test Dependencies

```kotlin
testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
testImplementation("com.h2database:h2:2.4.240")
testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.1")
```

- **JUnit 5** - Testing framework
- **H2 Database** - In-memory database for repository tests
- **Kotlin Test** - Kotlin extensions for JUnit

---

## Test Database Configuration

For integration tests, configure the database via environment variables:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/persondb"
export DB_USER="postgres"
export DB_PASSWORD="postgres"
./gradlew test
```

Or modify `TestDatabaseConfig` in `WebServerIntegrationTest.kt` directly.
