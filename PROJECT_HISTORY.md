# Project History & Milestones

A chronological record of all major changes made to the PersonDatabase project.

---

## Initial Setup

### Created: Kotlin + PostgreSQL Application

- Created CRUD operations for person records (name, age, profession, city)
- Built web interface accessible via browser
- Auto-seeded with 20 sample users on startup
- Tests for all functionality

---

## Database Configuration Fix

### Issue: PostgreSQL Default User on macOS

**Problem:** Connection was failing because macOS default PostgreSQL user is the macOS username (`tiagocollot`), not `postgres`.

**Solution:** Updated database configuration to use the correct default user:
```kotlin
val dbUser = System.getenv("DB_USER") ?: "postgres"  // Changed to use environment variable
```

---

## Gson LocalDateTime Serialization Fix

### Issue: Java 17+ Module Restrictions

**Problem:** Gson was unable to serialize `LocalDateTime` due to Java 17+ reflection restrictions.

**Solution:** Created custom `LocalDateTimeAdapter`:
```kotlin
class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) out.nullValue() else out.value(value.format(formatter))
    }
    
    override fun read(reader: JsonReader): LocalDateTime? {
        val value = reader.nextString()
        return if (value.isNullOrEmpty()) null else LocalDateTime.parse(value, formatter)
    }
}
```

---

## Auto-Seed on Restart

### Change: Re-seed Database Every Start

**Updated:** `seedDatabaseIfEmpty()` function in `WebServer.kt`

**Before:**
```kotlin
private fun seedDatabaseIfEmpty(service: PersonService) {
    if (service.getAllPeople().isEmpty()) {  // Only seed if empty
        // seed logic
    }
}
```

**After:**
```kotlin
private fun seedDatabaseIfEmpty(service: PersonService) {
    // Always seed - database is re-populated on every restart
    // seed logic
}
```

---

## Switched to Handlebars Templates

### Change: HTML File to HBS

**Before:** Used static `index.html` in `src/main/resources/public/`

**After:** Using Handlebars templates in `src/main/resources/templates/`

**Files Changed:**
- Deleted: `src/main/resources/public/index.html`
- Created: `src/main/resources/templates/index.hbs`

**Added Dependency** in `build.gradle.kts`:
```kotlin
implementation("com.github.jknack:handlebars:4.3.1")
```

**Updated `WebServer.kt`:**
```kotlin
val handlebars = Handlebars(ClassPathTemplateLoader("/templates", ".hbs"))
    .registerHelper("escape") { context: Any, _: Any? -> context.toString() }
    .registerHelper("formatDate") { context: Any, _: Any? ->
        if (context is String) {
            try { LocalDateTime.parse(context).toLocalDate().toString() }
            catch (e: Exception) { context }
        } else context.toString()
    }

get("/") { _, _ ->
    val people = service.getAllPeople()
    val template = handlebars.compile("index")
    template.apply(mapOf("people" to people))
}
```

---

## CSS Separation

### Change: External Stylesheet

**Before:** CSS was embedded in `index.html`

**After:** CSS moved to separate file `src/main/resources/public/styles.css`

**Files:**
- Created: `src/main/resources/public/styles.css`
- Updated: `src/main/resources/templates/index.hbs` (now links to stylesheet)

---

## Test Updates

### Changes to Integration Tests

**Removed:**
- 3 HTML page tests from `WebServerIntegrationTest` (testing static HTML content)

**Updated:**
- `WebServerMain` object to use Handlebars like the main server

**Added:**
- `HandlebarsTest.kt` - 13 new tests for template rendering:
  - Template loading from classpath
  - FormatDate helper functionality
  - HTML structure rendering
  - Form fields, table headers, buttons
  - External stylesheet linking
  - JavaScript for API calls

**Current Test Structure:**
| Test Class | Tests |
|------------|-------|
| PersonRepositoryTest.kt | 14 |
| PersonServiceTest.kt | 23 |
| HandlebarsTest.kt | 22 |
| RateLimiterTest.kt | 9 |
| SecurityTest.kt | 15 |
| WebServerIntegrationTest.kt | 10 |
| **Total** | **93** |

---

## Gradle Build Warnings Fix

### Issue: Java 17+ Native Module Warnings

**Problem:** Gradle was showing warnings about restricted methods in `java.lang.System`.

**Solution:**

1. **Upgraded Gradle** from 8.5 to 8.10 in `gradle-wrapper.properties`:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10-bin.zip
```

2. **Added JVM flag** to `gradlew`:
```bash
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m" "--enable-native-access=ALL-UNNAMED"'
```

3. **Created `gradle.properties`**:
```properties
org.gradle.jvmargs=--enable-native-access=ALL-UNNAMED
```

---

## README Updates

### Changes Made

1. **Database Deletion Warning** - Added highlighted warning section after Database Schema:
```markdown
> **⚠️ WARNING: Deleting the Database**
> 
> **If you delete the `persondb` database, all data will be permanently lost!**
```

2. **Project Structure Update** - Changed to reflect Handlebars:
```
src/main/resources/templates/
└── index.hbs             # Handlebars template for web interface
```

3. **Troubleshooting Section** - Added template not found error info.

---

## Dynamic Server-Side Rendering

### Change: Full Dynamic Web Interface

**Before:** Client-side JavaScript loaded data via API after page load

**After:** Server-side rendering with search and sort functionality

**New Features:**
- **Search** - Filter people by name, profession, or city
- **Sort** - Sort by any column (id, name, age, profession, city, created)
- **Order** - Ascending or descending order
- **Count Display** - Shows total and filtered count

**Updated `WebServer.kt` - GET `/` route:**
```kotlin
get("/") { req, _ ->
    val search = req.queryParams("search") ?: ""
    val sortBy = req.queryParams("sort") ?: "id"
    val order = req.queryParams("order") ?: "asc"

    var people = service.getAllPeople()

    // Filter by search
    if (search.isNotBlank()) {
        val searchLower = search.lowercase()
        people = people.filter {
            it.name.lowercase().contains(searchLower) ||
            it.profession.lowercase().contains(searchLower) ||
            it.city.lowercase().contains(searchLower)
        }
    }

    // Sort people
    people = when (sortBy) {
        "name" -> if (order == "asc") people.sortedBy { it.name } else people.sortedByDescending { it.name }
        "age" -> if (order == "asc") people.sortedBy { it.age } else people.sortedByDescending { it.age }
        // ... other columns
        else -> if (order == "asc") people.sortedBy { it.id } else people.sortedByDescending { it.id }
    }

    val totalCount = service.getAllPeople().size
    val filteredCount = people.size

    template.apply(mapOf(
        "people" to people,
        "search" to search,
        "sortBy" to sortBy,
        "order" to order,
        "totalCount" to totalCount,
        "filteredCount" to filteredCount,
        "isFiltered" to (search.isNotBlank() || sortBy != "id")
    ))
}
```

**Updated `index.hbs` template:**
```handlebars
{{#each people}}
<tr>
    <td>{{id}}</td>
    <td>{{name}}</td>
    <td>{{age}}</td>
    <td>{{profession}}</td>
    <td>{{city}}</td>
    <td>{{formatDate createdAt}}</td>
</tr>
{{/each}}
```

**URL Examples:**
| Feature | URL |
|---------|-----|
| Search | `/?search=developer` |
| Sort by name | `/?sort=name&order=asc` |
| Combined | `/?search=dev&sort=age&order=desc` |

---

## File Structure

```
src/main/kotlin/com/example/
├── Person.kt              # Data class
├── PersonRepository.kt    # Database operations (JDBC)
├── PersonService.kt       # Business logic with validation
├── DatabaseConfig.kt     # Database connection
├── Main.kt               # CLI demo
├── SeedDatabase.kt       # Populate 20 sample users
└── WebServer.kt          # Web interface (SparkJava + Handlebars)

src/main/resources/
├── public/
│   └── styles.css        # External stylesheet
└── templates/
    └── index.hbs         # Handlebars template

src/test/kotlin/com/example/
├── unit/                          # Unit tests (no external dependencies)
│   ├── PersonRepositoryTest.kt    # Repository unit tests (14)
│   └── PersonServiceTest.kt       # Service unit tests (23)
├── integration/                   # Integration tests (requires database)
│   └── WebServerIntegrationTest.kt # Integration tests (10)
├── security/                      # Security tests
│   ├── RateLimiterTest.kt         # Rate limiting tests (9)
│   └── SecurityTest.kt            # XSS/sanitization tests (15)
└── template/                     # Template engine tests
    └── HandlebarsTest.kt          # Template tests (22)
```

---

## Dependencies

```kotlin
dependencies {
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.jknack:handlebars:4.3.1")
    
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
    testImplementation("com.h2database:h2:2.2.224")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.1")
}
```

---

## Security Hardening

### Issue: XSS Vulnerabilities

**Problems Found:**
1. User data in `editPerson()` inline JavaScript handler was not escaped
2. Search query displayed without HTML escaping
3. No input length limits on user data

**Solutions Implemented:**

1. **JavaScript Escaping Helper** - Added `jsEscape` helper for inline JavaScript:
```kotlin
hbs.registerHelper("jsEscape", object : Helper<Any?> {
    override fun apply(context: Any?, options: Options): Any {
        val str = context?.toString() ?: return ""
        return str
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            // ...
    }
})
```

2. **HTML Escaping Helper** - Added `htmlEscape` helper for displaying user content:
```kotlin
hbs.registerHelper("htmlEscape", object : Helper<Any?> {
    override fun apply(context: Any?, options: Options): Any {
        val str = context?.toString() ?: return ""
        return str
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            // ...
    }
})
```

3. **Input Sanitization** - Added server-side sanitization:
```kotlin
private fun sanitizeSearchQuery(input: String): String {
    return input
        .take(100)  // Max length
        .replace(Regex("[<>\"'&]"), "")  // Remove dangerous chars
        .trim()
}

private fun sanitizeSortParam(input: String): String {
    return when (input.lowercase()) {
        "id", "name", "age", "profession", "city", "created" -> input.lowercase()
        else -> "id"  // Whitelist validation
    }
}
```

4. **Input Validation** - Added max length and age validation:
```kotlin
const val MAX_NAME_LENGTH = 255
const val MAX_PROFESSION_LENGTH = 255
const val MAX_CITY_LENGTH = 255
const val MAX_AGE = 150

require(age <= MAX_AGE) { "Age cannot exceed $MAX_AGE" }
```

5. **Form Input Limits** - Added HTML5 validation:
```html
<input type="text" name="name" maxlength="255" required>
<input type="number" name="age" min="0" max="150" required>
```

**Tests Added:**
- `jsEscape helper escapes quotes and prevents XSS`
- `jsEscape helper escapes double quotes`
- `jsEscape helper escapes newlines`
- `htmlEscape helper escapes special characters`
- `createPerson throws when age exceeds maximum`

---

## Additional Security Hardening

### Issue: Missing Security Headers and Rate Limiting

**Problems Found:**
1. No security headers to protect against clickjacking, XSS, etc.
2. No rate limiting - API could be abused
3. No global error handling - stack traces could be exposed

**Solutions Implemented:**

1. **Security Headers** - Added via `addSecurityHeaders()`:
```kotlin
res.header("X-Frame-Options", "DENY")
res.header("X-Content-Type-Options", "nosniff")
res.header("X-XSS-Protection", "1; mode=block")
res.header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
res.header("Content-Security-Policy", "default-src 'self'; ...")
res.header("Referrer-Policy", "strict-origin-when-cross-origin")
res.header("Cache-Control", "no-store, no-cache, must-revalidate")
```

2. **Rate Limiting** - Added via `RateLimiter` object:
```kotlin
object RateLimiter {
    private const val MAX_REQUESTS = 100
    private const val WINDOW_MS = 60_000L  // 1 minute
    
    fun isAllowed(ip: String): Boolean { ... }
    fun getRemaining(ip: String): Int { ... }
    fun getResetTime(ip: String): Long { ... }
}
```

3. **Global Error Handling** - Added `/error` endpoint for 500 errors:
```kotlin
get("/error") { _, res ->
    res.status(500)
    gson.toJson(mapOf("error" to "An unexpected error occurred."))
}
```

4. **Rate Limit Headers** - API responses include:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 45
```

---

## Current Status

- **All 93 tests passing**
- **Web server running on port 4567**
- **Auto-seed on every restart**
- **Handlebars templates with dynamic server-side rendering**
- **Search and sort functionality**
- **External CSS stylesheet**
- **No build warnings**
- **Security hardened with XSS protection**
- **Security headers configured (CSP, X-Frame-Options, etc.)**
- **Rate limiting implemented (100 req/min)**
- **Global error handling**
