# PersonDatabase

A Kotlin application with PostgreSQL for managing person records using raw JDBC.

## About This Project

All features were developed using TDD:
- Tests written first
- Implementation follows to make tests pass
- Refactoring for maintainability

## Prerequisites

- **JDK 17+** - [Install](https://adoptium.net/)
- **PostgreSQL** - [Install](https://www.postgresql.org/download/)
- **pgAdmin 4** - [Download](https://www.pgadmin.org/download/)
- **Gradle 8.10** (wrapper included)

## Setup

### 1. Start PostgreSQL

**macOS (Homebrew):**
```bash
brew services start postgresql@16
```

**Linux:**
```bash
sudo systemctl start postgresql
```

**Windows:**
Start PostgreSQL service from Services app.

### 2. Create Database and User

**For macOS (Homebrew) - Run these commands:**
```bash
# Find your macOS username
whoami

# Create postgres database and user (replace YOUR_USERNAME with the output from whoami)
createdb -U YOUR_USERNAME postgres
psql -U YOUR_USERNAME -d postgres -c "CREATE ROLE postgres WITH LOGIN SUPERUSER PASSWORD 'postgres';"
psql -U YOUR_USERNAME -d postgres -c "CREATE DATABASE persondb OWNER postgres;"
```

**For Linux/Windows (or if postgres user already exists):**
```bash
psql -U postgres -c "CREATE DATABASE persondb;"
psql -U postgres -c "ALTER USER postgres WITH PASSWORD 'postgres';"
```

### 3. Verify Setup

```bash
psql -U postgres -d persondb -c "SELECT 1;"
```

You should see:
```
 ?column?
 ----------
        1
(1 row)
```

## Running the Application

### Web Interface (Recommended)

1. Build the project:
   ```bash
   cd ~/Desktop/PersonDatabase
   ./gradlew build
   ```

2. Run the web server:
   ```bash
   ./gradlew run
   ```

3. Open your browser to: **http://localhost:4567**

The web interface allows you to:
- **View** all people in a table
- **Search** people by name, profession, or city
- **Sort** by any column (ID, name, age, profession, city, created date)
- **Add** new people with a form
- **Edit** existing people
- **Delete** people
- See **created_at** timestamps

### Search and Sort

The web interface supports server-side filtering and sorting:

| Feature | URL Example |
|---------|-------------|
| Search | `http://localhost:4567/?search=developer` |
| Sort by name | `http://localhost:4567/?sort=name&order=asc` |
| Combined | `http://localhost:4567/?search=dev&sort=age&order=desc` |

**Sort parameters:**
- `sort` - Column to sort by: `id`, `name`, `age`, `profession`, `city`, `created`
- `order` - Sort order: `asc` or `desc`

### CLI Demo

To run the command-line demo instead:

1. Edit `build.gradle.kts` and change:
   ```kotlin
   application {
       mainClass.set("com.example.MainKt")
   }
   ```

2. Run:
   ```bash
   ./gradlew run
   ```

### Seed Database with 20 Sample Users

To populate the database with 20 sample people:

1. Edit `build.gradle.kts` and change:
   ```kotlin
   application {
       mainClass.set("com.example.SeedDatabaseKt")
   }
   ```

2. Run:
   ```bash
   ./gradlew run
   ```

### REST API Endpoints

When running the web server, the following API endpoints are available:

| Method | Endpoint            | Description          |
|--------|---------------------|----------------------|
| GET    | `/`                | Web interface        |
| GET    | `/api/people`       | Get all people       |
| GET    | `/api/people/:id`   | Get person by ID    |
| POST   | `/api/people`       | Create new person   |
| PUT    | `/api/people/:id`   | Update person        |
| DELETE | `/api/people/:id`   | Delete person       |

**Example API usage:**
```bash
# Get all people
curl http://localhost:4567/api/people

# Create a person
curl -X POST http://localhost:4567/api/people \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","age":30,"profession":"Developer","city":"NYC"}'

# Update a person
curl -X PUT http://localhost:4567/api/people/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","age":31,"profession":"Senior Dev","city":"LA"}'

# Delete a person
curl -X DELETE http://localhost:4567/api/people/1
```

## Using pgAdmin 4

### 1. Add PostgreSQL Server

1. Open pgAdmin 4
2. Right-click on **Servers** in the left sidebar
3. Select **Create** > **Server...**

### 2. Configure Server Connection

1. In the **Create - Server** dialog, go to the **General** tab:
   - **Name:** `Local PostgreSQL`

2. Go to the **Connection** tab:
   - **Host name/address:** `localhost`
   - **Port:** `5432`
   - **Maintenance database:** `postgres`
   - **Username:** `postgres`
   - **Password:** `postgres`
   - Check **Save password?**

3. Click **Save**

### 3. Create Database (via pgAdmin - if not created via terminal)

1. Expand **Servers** > **Local PostgreSQL**
2. Right-click on **Databases**
3. Select **Create** > **Database...**
4. In the **General** tab:
   - **Database:** `persondb`
   - **Owner:** `postgres`
5. Click **Save**

### 4. View Your Tables

1. Expand:
   - **Servers** > **Local PostgreSQL** > **Databases** > **persondb** > **Schemas** > **public** > **Tables**

2. You should see the `people` table listed

### 5. View/Edit Table Data

**Option A - View/Edit Data:**
1. Right-click on the `people` table
2. Select **View/Edit Data** > **All Rows**
3. A new tab opens showing all rows in the table

**Option B - Query Tool:**
1. Right-click on the `people` table
2. Select **Query Tool**
3. Type and execute queries:
   ```sql
   SELECT * FROM people;
   ```

**Option C - Using psql:**
```bash
psql -U postgres -d persondb -c "SELECT * FROM people;"
```

## Using psql (CLI)

Connect to database:
```bash
psql -U postgres -d persondb
```

### Create (INSERT)
```sql
INSERT INTO people (name, age, profession, city) 
VALUES ('John Doe', 30, 'Developer', 'New York');
```

### Read (SELECT)
```sql
-- All people
SELECT * FROM people;

-- Single person by id
SELECT * FROM people WHERE id = 1;

-- Filter by city
SELECT * FROM people WHERE city = 'New York';

-- Order by creation date (newest first)
SELECT * FROM people ORDER BY created_at DESC;
```

### Update
```sql
UPDATE people 
SET name = 'Jane Doe', age = 31, profession = 'Senior Developer', city = 'Boston'
WHERE id = 1;
```

### Delete
```sql
-- Delete single person
DELETE FROM people WHERE id = 1;

-- Delete all
DELETE FROM people;
```

### Exit psql
```sql
\q
```

## Project Structure

```
src/main/kotlin/com/example/
├── Person.kt              # Data class
├── PersonRepository.kt    # Database operations (JDBC)
├── PersonService.kt      # Business logic with validation
├── DatabaseConfig.kt     # Database connection
├── Main.kt               # CLI demo
├── SeedDatabase.kt       # Populate 20 sample users
└── WebServer.kt          # Web interface (SparkJava + Handlebars)

src/main/resources/
├── public/
│   └── styles.css        # External stylesheet
└── templates/
    └── index.hbs         # Handlebars template for web interface

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

## Database Schema

> **⚠️ WARNING: Deleting the Database**
> 
> **If you delete the `persondb` database, all data will be permanently lost!** 
> 
> The application will automatically re-seed the database with 20 sample users when restarted, but any custom data you added will be gone.
> 
> To delete the database:
> ```bash
> # Stop the application first!
> dropdb -U postgres persondb
> ```
> 
> To recreate after deletion:
> ```bash
> # Recreate the database
> psql -U postgres -c "CREATE DATABASE persondb OWNER postgres;"
> 
> # Start the application - it will auto-seed the data
> ./gradlew run
> ```

```sql
CREATE TABLE people (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INTEGER NOT NULL,
    profession VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Person Fields

| Field       | Type     | Description                   |
|-------------|----------|-------------------------------|
| id          | INTEGER  | Auto-generated primary key    |
| name        | VARCHAR  | Person's full name            |
| age         | INTEGER  | Person's age                  |
| profession  | VARCHAR  | Job title                     |
| city        | VARCHAR  | City of residence             |
| created_at  | TIMESTAMP| When the record was created   |

## Running Tests

```bash
./gradlew test
```

**Test Summary:**
| Folder | Test Class | Tests |
|--------|------------|-------|
| unit/ | PersonRepositoryTest.kt | 14 |
| unit/ | PersonServiceTest.kt | 23 |
| template/ | HandlebarsTest.kt | 22 |
| security/ | RateLimiterTest.kt | 9 |
| security/ | SecurityTest.kt | 15 |
| integration/ | WebServerIntegrationTest.kt | 10 |
| | **Total** | **93** |

**Note:** Integration tests require PostgreSQL running. Ensure the database is available before running tests.

## Troubleshooting

**"role postgres does not exist":**
- On macOS with Homebrew, the default user is your macOS username
- Run: `whoami` to find it
- Then create the postgres role (replace YOUR_USERNAME with the output from whoami):
  ```bash
  createdb -U YOUR_USERNAME postgres
  psql -U YOUR_USERNAME -d postgres -c "CREATE ROLE postgres WITH LOGIN SUPERUSER PASSWORD 'postgres';"
  psql -U YOUR_USERNAME -d postgres -c "CREATE DATABASE persondb OWNER postgres;"
  ```

**pgAdmin shows "Could not connect to server":**
- Ensure PostgreSQL is running: `brew services list` (macOS)
- Check the PostgreSQL service is started

**Connection timeout in pgAdmin:**
- Verify host is `localhost` and port is `5432`
- Check PostgreSQL is accepting TCP/IP connections

**Authentication failed:**
- Make sure to enter the correct password (`postgres`) in pgAdmin
- If using macOS, ensure the postgres role was created with the correct password

**Database not found:**
- Create it using the commands in section 2
- Or create via pgAdmin (see steps above)

**Table not visible:**
- Right-click on **Tables** and select **Refresh**
- Make sure you have selected the correct database (`persondb`)

**Web server port already in use:**
- Stop any running instances: `lsof -ti:4567 | xargs kill 2>/dev/null || true`
- Or change the port in `WebServer.kt` (line 46: `port(4567)`)

**Template not found error:**
- Ensure Handlebars templates are in `src/main/resources/templates/`
- Template files must have `.hbs` extension
