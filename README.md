# PersonDatabase

A Kotlin application with PostgreSQL for managing person records using raw JDBC.

## About This Project

This project was created using **[OpenCode](https://opencode.ai)** with the **big-pickle** agent, following **Test-Driven Development (TDD)** methodology.

All features were developed using TDD:
- Tests written first
- Implementation follows to make tests pass
- Refactoring for maintainability

## Prerequisites

- **JDK 17+** - [Install](https://adoptium.net/)
- **PostgreSQL** - [Install](https://www.postgresql.org/download/)
- **pgAdmin 4** - [Download](https://www.pgadmin.org/download/)
- **Gradle 8+** (wrapper included)

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

### 4. Build & Run

```bash
cd ~/Desktop/PersonDatabase
./gradlew build
./gradlew run
```

**To populate with 20 sample users:** Edit `build.gradle.kts` and change:
```kotlin
application {
    mainClass.set("com.example.SeedDatabaseKt")
}
```
Then run `./gradlew run`.

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
├── PersonService.kt       # Business logic with validation
├── DatabaseConfig.kt      # Database connection
├── Main.kt                # CLI demo
└── SeedDatabase.kt        # Populate 20 sample users

src/test/kotlin/com/example/
├── PersonRepositoryTest.kt # Repository tests
└── PersonServiceTest.kt    # Service tests
```

## Database Schema

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
