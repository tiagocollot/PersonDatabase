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
| PersonServiceTest.kt | 22 |
| HandlebarsTest.kt | 13 |
| WebServerIntegrationTest.kt | 11 |
| **Total** | **60** |

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
├── PersonRepositoryTest.kt        # Repository unit tests (14)
├── PersonServiceTest.kt           # Service unit tests (22)
├── HandlebarsTest.kt             # Template tests (13)
└── WebServerIntegrationTest.kt    # Integration tests (11)
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
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}
```

---

## Current Status

- **All 60 tests passing**
- **Web server running on port 4567**
- **Auto-seed on every restart**
- **Handlebars templates for HTML rendering**
- **External CSS stylesheet**
- **No build warnings**
