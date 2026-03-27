# Test Coverage Summary

This document outlines all test categories and their coverage.

---

## Test Overview

| Test Class | Type | Count | Purpose |
|------------|------|-------|---------|
| PersonRepositoryTest.kt | Unit | 14 | Test data layer with in-memory repository |
| PersonServiceTest.kt | Unit | 22 | Test business logic with mocked repository |
| HandlebarsTest.kt | Unit | 13 | Test Handlebars template rendering |
| WebServerIntegrationTest.kt | Integration | 11 | Test full HTTP endpoints with database |
| **Total** | | **60** | |

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

## 2. PersonServiceTest.kt (22 Tests)

**Purpose:** Unit tests for business logic layer using a mock repository.

### Test Coverage

| Category | Tests | Description |
|----------|-------|-------------|
| **Create - Valid** | 2 | Creating persons with valid data |
| **Create - Name Validation** | 2 | Rejecting blank/empty names |
| **Create - Age Validation** | 2 | Rejecting negative ages, accepting zero |
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

## 3. HandlebarsTest.kt (13 Tests)

**Purpose:** Unit tests for Handlebars template engine and custom helpers.

### Test Coverage

| Category | Tests | Description |
|----------|-------|-------------|
| **Template Loading** | 1 | Loading templates from classpath |
| **FormatDate Helper** | 3 | Date formatting functionality |
| **HTML Structure** | 1 | Verifying HTML renders correctly |
| **Form Elements** | 1 | Verifying all form fields present |
| **Action Buttons** | 1 | Verifying Edit/Delete buttons |
| **Table Headers** | 1 | Verifying table structure |
| **Stylesheet** | 1 | Verifying CSS link present |
| **JavaScript** | 1 | Verifying API call handlers |
| **Client-Side Rendering** | 1 | Verifying empty table body |
| **Form Handlers** | 1 | Verifying form event listeners |
| **Sanitization** | 1 | Verifying escapeHtml function |

### Specific Tests

```
formatDate helper converts ISO datetime to date
formatDate helper handles invalid datetime
formatDate helper handles non-string input
template loads from classpath
template renders HTML structure
template renders all required form fields
template includes Edit and Delete buttons
template includes table headers
template links to external stylesheet
template includes JavaScript for API calls
template has empty table body for client-side rendering
template includes add form handler
template includes escapeHtml function for client-side sanitization
```

### Custom Helpers Tested

- `formatDate` - Converts ISO datetime strings to date format
- `escape` - HTML escaping for user input

---

## 4. WebServerIntegrationTest.kt (11 Tests)

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

### Requirements

- PostgreSQL must be running
- Database `persondb` must exist
- User credentials must be configured

---

## Test Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     WebServer.kt                             │
│                    (Spark Server)                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  WebServerIntegrationTest.kt                 │
│                   (Integration Tests)                       │
│              Tests full HTTP endpoints                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    PersonService.kt                          │
│                  (Business Logic)                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  PersonRepository.kt                        │
│                   (Data Access)                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 PersonServiceTest.kt                        │
│         (Unit Tests - Mock Repository)                      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              PersonRepositoryTest.kt                        │
│          (Unit Tests - In-Memory Repository)               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   HandlebarsTest.kt                        │
│              (Unit Tests - Template Engine)                 │
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
./gradlew test --tests "com.example.PersonServiceTest"
./gradlew test --tests "com.example.PersonRepositoryTest"
./gradlew test --tests "com.example.HandlebarsTest"
./gradlew test --tests "com.example.WebServerIntegrationTest"
```

### Run Unit Tests Only (No Database Required)
```bash
./gradlew test --tests "com.example.PersonServiceTest"
./gradlew test --tests "com.example.PersonRepositoryTest"
./gradlew test --tests "com.example.HandlebarsTest"
```

### Run Integration Tests (Requires Database)
```bash
./gradlew test --tests "com.example.WebServerIntegrationTest"
```

---

## Test Dependencies

```kotlin
testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
testImplementation("com.h2database:h2:2.2.224")
testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
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
