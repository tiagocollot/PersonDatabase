# PersonDatabase Project - TODO List

A comprehensive step-by-step guide to creating a Kotlin + PostgreSQL web application from scratch.

---

## Phase 1: Project Setup

### 1.1 Initialize Gradle Project
- [ ] Create project directory
- [ ] Initialize Gradle wrapper: `gradle wrapper --gradle-version 8.10`
- [ ] Create `settings.gradle.kts` with project name
- [ ] Create basic `build.gradle.kts` with Kotlin JVM plugin
- [ ] Verify Gradle build works: `./gradlew build`

### 1.2 Configure Kotlin
- [ ] Add Kotlin JVM dependency to `build.gradle.kts`
- [ ] Set JVM toolchain to Java 17
- [ ] Configure application plugin with main class
- [ ] Test compilation: `./gradlew compileKotlin`

### 1.3 Add Dependencies
Add these to `build.gradle.kts`:
```kotlin
dependencies {
    implementation("org.postgresql:postgresql:42.7.10")
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.github.jknack:handlebars:4.3.1")
    
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
    testImplementation("com.h2database:h2:2.4.240")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}
```
- [ ] Add all dependencies
- [ ] Test dependencies resolve: `./gradlew dependencies`

---

## Phase 2: Database Layer

### 2.1 Database Configuration
- [ ] Create `DatabaseConfig.kt` object
- [ ] Implement `connect(url, user, password)` function
- [ ] Implement `getConnection()` function
- [ ] Implement `initSchema()` to create `people` table
- [ ] Implement `close()` function

### 2.2 Person Data Class
- [ ] Create `Person.kt` data class with:
  - `id: Int = 0`
  - `name: String`
  - `age: Int`
  - `profession: String`
  - `city: String`
  - `createdAt: LocalDateTime? = null`

### 2.3 Repository Interface
- [ ] Create `PersonRepositoryInterface`
- [ ] Define methods: `save`, `findById`, `findAll`, `update`, `delete`

### 2.4 Repository Implementation
- [ ] Create `PersonRepository.kt` implementing the interface
- [ ] Use JDBC PreparedStatement for SQL queries
- [ ] Implement `save` with RETURNING clause for id and created_at
- [ ] Implement `findById`, `findAll`, `update`, `delete`
- [ ] Add `ResultSet.toPerson()` extension function

---

## Phase 3: Business Logic Layer

### 3.1 Service Class
- [ ] Create `PersonService.kt`
- [ ] Add constructor with repository dependency
- [ ] Implement CRUD methods delegating to repository

### 3.2 Validation
- [ ] Add validation for name (not blank)
- [ ] Add validation for age (>= 0, <= 150)
- [ ] Add validation for profession (not blank)
- [ ] Add validation for city (not blank)
- [ ] Implement whitespace trimming for all string inputs

### 3.3 Constants
- [ ] Create `Constants.kt` with:
  - Database defaults
  - Table/column names
  - Validation messages
  - Server configuration

---

## Phase 4: Web Server

### 4.1 Basic Server Setup
- [ ] Create `WebServer.kt` with main function
- [ ] Initialize database connection
- [ ] Initialize schema
- [ ] Create repository and service instances
- [ ] Configure Spark port 4567
- [ ] Configure static files location

### 4.2 REST API Endpoints
- [ ] `GET /api/people` - Return all people
- [ ] `GET /api/people/:id` - Return single person
- [ ] `POST /api/people` - Create person
- [ ] `PUT /api/people/:id` - Update person
- [ ] `DELETE /api/people/:id` - Delete person

### 4.3 JSON Serialization
- [ ] Create `GsonBuilder` with custom config
- [ ] Create `LocalDateTimeAdapter` for Gson
- [ ] Configure JSON responses with success/error structure

### 4.4 Error Handling
- [ ] Handle validation errors with proper messages
- [ ] Handle not found cases
- [ ] Handle invalid ID format

---

## Phase 5: Handlebars Templates

### 5.1 Template Setup
- [ ] Create `src/main/resources/templates/` directory
- [ ] Create `index.hbs` template
- [ ] Configure `ClassPathTemplateLoader` in WebServer

### 5.2 Template Helpers
- [ ] Register `formatDate` helper for LocalDateTime
- [ ] Register `htmlEscape` helper for XSS prevention
- [ ] Register `jsEscape` helper for inline JS
- [ ] Register `eq` helper for conditional rendering

### 5.3 Dynamic Web Interface
- [ ] Implement server-side rendering of people list
- [ ] Add search functionality (filter by name, profession, city)
- [ ] Add sort functionality (all columns)
- [ ] Add order toggle (asc/desc)
- [ ] Add pagination/count display

### 5.4 HTML Structure
- [ ] Create form for adding new people
- [ ] Create table with sortable columns
- [ ] Create edit form (hidden by default)
- [ ] Create empty state messages
- [ ] Add success/error message displays

---

## Phase 6: Frontend

### 6.1 Stylesheet
- [ ] Create `src/main/resources/public/styles.css`
- [ ] Add form styling
- [ ] Add table styling
- [ ] Add button styles
- [ ] Add responsive design
- [ ] Link stylesheet in template

### 6.2 JavaScript
- [ ] Add form submission handlers
- [ ] Add edit/delete functionality
- [ ] Add success/error message display
- [ ] Add page reload after mutations

---

## Phase 7: Testing

### 7.1 Test Setup
- [ ] Configure JUnit 5 in `build.gradle.kts`
- [ ] Set up test resources
- [ ] Configure test task with JUnitPlatform

### 7.2 Repository Tests
- [ ] Create `InMemoryPersonRepository` for testing
- [ ] Write tests for `save` operation
- [ ] Write tests for `findById` (found/not found)
- [ ] Write tests for `findAll` (empty/non-empty)
- [ ] Write tests for `update` (success/not found)
- [ ] Write tests for `delete` (success/not found)

### 7.3 Service Tests
- [ ] Create `MockPersonRepository` for testing
- [ ] Write tests for valid person creation
- [ ] Write tests for validation (name, age, profession, city)
- [ ] Write tests for update operations
- [ ] Write tests for delete operations

### 7.4 Handlebars Tests
- [ ] Write tests for custom helpers (formatDate, jsEscape, htmlEscape)
- [ ] Write tests for template rendering
- [ ] Write tests for dynamic content
- [ ] Write tests for empty states

### 7.5 Integration Tests
- [ ] Create test server setup
- [ ] Write tests for all API endpoints
- [ ] Add database cleanup in teardown

---

## Phase 8: Security

### 8.1 Input Sanitization
- [ ] Sanitize search query input (remove dangerous chars)
- [ ] Validate sort parameters (whitelist)
- [ ] Limit input lengths
- [ ] Add max age validation

### 8.2 XSS Prevention
- [ ] Escape HTML in template output
- [ ] Escape JavaScript in inline handlers
- [ ] Escape search query in display
- [ ] Test with malicious inputs

### 8.3 Form Validation
- [ ] Add HTML5 validation attributes
- [ ] Add server-side validation
- [ ] Sanitize all user inputs

---

## Phase 9: Database Setup

### 9.1 PostgreSQL Installation
- [ ] Install PostgreSQL (Homebrew/Linux/Windows)
- [ ] Start PostgreSQL service
- [ ] Create database: `createdb persondb`

### 9.2 User Configuration
- [ ] Create PostgreSQL user or use existing
- [ ] Set appropriate permissions
- [ ] Test connection: `psql -U user -d persondb`

### 9.3 Environment Variables (Optional)
- [ ] Set `DB_URL` for custom database URL
- [ ] Set `DB_USER` for custom username
- [ ] Set `DB_PASSWORD` for custom password

---

## Phase 10: Seeding

### 10.1 Seed Data
- [ ] Create `SeedDatabase.kt` entry point
- [ ] Create list of 20 sample people
- [ ] Implement seeding logic
- [ ] Test seeding runs successfully

### 10.2 Auto-Seed on Startup
- [ ] Add seeding function to WebServer
- [ ] Call seeding on application start
- [ ] Configure to seed every restart (as per requirements)

---

## Phase 11: Documentation

### 11.1 README
- [ ] Create comprehensive README.md
- [ ] Document prerequisites
- [ ] Document setup instructions
- [ ] Document running the application
- [ ] Document REST API
- [ ] Document troubleshooting

### 11.2 Project History
- [ ] Create PROJECT_HISTORY.md
- [ ] Document all milestones
- [ ] Record bug fixes and decisions

### 11.3 Test Coverage
- [ ] Create TEST_COVERAGE_SUMMARY.md
- [ ] Document all test categories
- [ ] List specific tests
- [ ] Document test architecture

---

## Phase 12: Build Configuration

### 12.1 Gradle Properties
- [ ] Create `gradle.properties`
- [ ] Add JVM arguments for Java 17 compatibility
- [ ] Configure Gradle options

### 12.2 Gradle Wrapper
- [ ] Upgrade to Gradle 8.10 in `gradle-wrapper.properties`
- [ ] Add JVM flags to `gradlew` script
- [ ] Test with `--enable-native-access=ALL-UNNAMED`

### 12.3 .gitignore
- [ ] Create `.gitignore` file
- [ ] Ignore build artifacts
- [ ] Ignore IDE files
- [ ] Ignore Gradle cache

---

## Phase 13: Final Verification

### 13.1 Build Verification
- [ ] Run `./gradlew clean build`
- [ ] Verify all tests pass
- [ ] Check for deprecation warnings

### 13.2 Runtime Verification
- [ ] Start application: `./gradlew run`
- [ ] Test web interface at http://localhost:4567
- [ ] Test all CRUD operations
- [ ] Test search and sort
- [ ] Test API endpoints with curl

### 13.3 Security Verification
- [ ] Test with XSS payloads
- [ ] Verify sanitization works
- [ ] Verify validation catches invalid inputs

---

## Quick Reference

### Commands
```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Run application
./gradlew run

# Clean build
./gradlew clean build

# Upgrade Gradle wrapper
./gradlew wrapper --gradle-version 8.10
```

### File Structure
```
src/main/kotlin/com/example/
├── Person.kt
├── PersonRepository.kt
├── PersonService.kt
├── DatabaseConfig.kt
├── WebServer.kt
├── Main.kt
└── SeedDatabase.kt

src/main/resources/
├── public/
│   └── styles.css
└── templates/
    └── index.hbs

src/test/kotlin/com/example/
├── unit/                          # Unit tests
│   ├── PersonRepositoryTest.kt
│   └── PersonServiceTest.kt
├── integration/                   # Integration tests
│   └── WebServerIntegrationTest.kt
├── security/                     # Security tests
│   ├── RateLimiterTest.kt
│   └── SecurityTest.kt
└── template/                    # Template tests
    └── HandlebarsTest.kt
```

### Dependencies
- Kotlin 1.9.22
- PostgreSQL Driver 42.7.10
- SparkJava 2.9.4
- Gson 2.13.2
- Handlebars 4.3.1
- JUnit 5.10.1
- H2 Database 2.4.240

---

## Tips

1. **Start Small** - Get the database connection working first
2. **Test Incrementally** - Write tests as you build each component
3. **Handle Exceptions** - Always handle database and network errors
4. **Validate Input** - Never trust user input
5. **Escape Output** - Prevent XSS at every output point
6. **Use PreparedStatements** - Prevent SQL injection
7. **Document Decisions** - Record why you made certain choices
