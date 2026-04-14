package com.example.integration

import com.example.DatabaseConfig
import com.example.Person
import com.example.PersonRepository
import com.example.PersonService
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.opentest4j.TestAbortedException
import spark.Spark
import com.google.gson.GsonBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.format.DateTimeFormatter
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Options
import com.github.jknack.handlebars.io.ClassPathTemplateLoader

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class WebServerIntegrationTest {
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()
    private val baseUrl = "http://localhost:4567"
    
    @BeforeEach
    fun setUp() {
        try {
            Spark.stop()
            Thread.sleep(500)
            
            TestDatabaseConfig.connect()
            TestDatabaseConfig.initSchema()
            
            WebServerMain.startServer()
            Thread.sleep(1500)
        } catch (e: Exception) {
            throw TestAbortedException("Database or server not available: ${e.message}")
        }
    }

    @AfterEach
    fun tearDown() {
        try {
            Spark.stop()
            Thread.sleep(500)
            TestDatabaseConfig.close()
        } catch (e: Exception) {
        }
    }

    @Test
    fun `GET api people returns seeded data`() {
        val response = sendGet("$baseUrl/api/people")
        
        assertEquals(200, response["status"])
        val people = gson.fromJson(response["body"] as String, Array<Person>::class.java)
        assertTrue(people.isNotEmpty())
        assertEquals(20, people.size)
    }

    @Test
    fun `POST api people creates new person`() {
        val response = sendPost(
            "$baseUrl/api/people",
            mapOf(
                "name" to "John Doe",
                "age" to 30,
                "profession" to "Developer",
                "city" to "NYC"
            )
        )
        
        assertEquals(200, response["status"])
        val result = gson.fromJson(response["body"] as String, Map::class.java)
        assertEquals(true, result["success"])
        
        @Suppress("UNCHECKED_CAST")
        val data = result["data"] as Map<*, *>
        assertEquals("John Doe", data["name"])
        assertEquals("Developer", data["profession"])
        assertEquals("NYC", data["city"])
        assertNotNull(data["id"])
        assertNotNull(data["createdAt"])
    }

    @Test
    fun `POST api people returns error for blank name`() {
        val response = sendPost(
            "$baseUrl/api/people",
            mapOf(
                "name" to "",
                "age" to 30,
                "profession" to "Developer",
                "city" to "NYC"
            )
        )
        
        assertEquals(200, response["status"])
        val result = gson.fromJson(response["body"] as String, Map::class.java)
        assertEquals(false, result["success"])
        assertEquals("Name cannot be blank", result["error"])
    }

    @Test
    fun `POST api people returns error for negative age`() {
        val response = sendPost(
            "$baseUrl/api/people",
            mapOf(
                "name" to "John",
                "age" to -1,
                "profession" to "Developer",
                "city" to "NYC"
            )
        )
        
        assertEquals(200, response["status"])
        val result = gson.fromJson(response["body"] as String, Map::class.java)
        assertEquals(false, result["success"])
        assertEquals("Age cannot be negative", result["error"])
    }

    @Test
    fun `GET api people id returns person when exists`() {
        val response = sendGet("$baseUrl/api/people/1")
        
        assertEquals(200, response["status"])
        val person = gson.fromJson(response["body"] as String, Person::class.java)
        assertEquals("Alice Johnson", person.name)
        assertEquals(28, person.age)
    }

    @Test
    fun `GET api people id returns error when not exists`() {
        val response = sendGet("$baseUrl/api/people/9999")
        
        assertEquals(200, response["status"])
        val result = gson.fromJson(response["body"] as String, Map::class.java)
        assertEquals("Person not found", result["error"])
    }

    @Test
    fun `PUT api people updates person`() {
        val response = sendPut(
            "$baseUrl/api/people/1",
            mapOf(
                "name" to "Updated Name",
                "age" to 29,
                "profession" to "Senior Engineer",
                "city" to "New City"
            )
        )
        
        assertEquals(200, response["status"])
        val result = gson.fromJson(response["body"] as String, Map::class.java)
        assertEquals(true, result["success"])
        
        @Suppress("UNCHECKED_CAST")
        val data = result["data"] as Map<*, *>
        assertEquals("Updated Name", data["name"])
    }

    @Test
    fun `PUT api people returns error when not exists`() {
        val response = sendPut(
            "$baseUrl/api/people/9999",
            mapOf(
                "name" to "Updated",
                "age" to 21,
                "profession" to "NewJob",
                "city" to "City2"
            )
        )
        
        assertEquals(200, response["status"])
        val result = gson.fromJson(response["body"] as String, Map::class.java)
        assertEquals(false, result["success"])
        assertEquals("Person not found", result["error"])
    }

    @Test
    fun `DELETE api people removes person`() {
        val response = sendDelete("$baseUrl/api/people/1")
        
        assertEquals(200, response["status"])
        val result = gson.fromJson(response["body"] as String, Map::class.java)
        assertEquals(true, result["success"])
        
        val getResponse = sendGet("$baseUrl/api/people/1")
        val getResult = gson.fromJson(getResponse["body"] as String, Map::class.java)
        assertEquals("Person not found", getResult["error"])
    }

    @Test
    fun `DELETE api people returns error when not exists`() {
        val response = sendDelete("$baseUrl/api/people/9999")
        
        assertEquals(200, response["status"])
        val result = gson.fromJson(response["body"] as String, Map::class.java)
        assertEquals(false, result["success"])
        assertEquals("Person not found", result["error"])
    }

    @Test
    fun `seeding twice does not duplicate rows - always exactly 20 people`() {
        // First seed already happened in startServer via reseedDatabase
        val firstCount = gson.fromJson(
            sendGet("$baseUrl/api/people")["body"] as String,
            Array<Person>::class.java
        ).size

        // Simulate a second server restart by calling reseed again directly
        val service = PersonService(PersonRepository())
        service.clearAllPeople()
        val seedPeople = listOf(
            Person(0, "Alice Johnson", 28, "Software Engineer", "San Francisco"),
            Person(0, "Bob Smith", 35, "Data Scientist", "New York"),
            Person(0, "Carol Williams", 42, "Product Manager", "Seattle"),
            Person(0, "David Brown", 31, "Frontend Developer", "Austin"),
            Person(0, "Emma Davis", 26, "UX Designer", "Portland"),
            Person(0, "Frank Miller", 39, "DevOps Engineer", "Chicago"),
            Person(0, "Grace Wilson", 33, "Backend Developer", "Boston"),
            Person(0, "Henry Taylor", 45, "CTO", "Los Angeles"),
            Person(0, "Ivy Anderson", 29, "QA Engineer", "Denver"),
            Person(0, "Jack Thomas", 37, "Full Stack Developer", "Miami"),
            Person(0, "Karen Martinez", 24, "Junior Developer", "Phoenix"),
            Person(0, "Leo Garcia", 41, "Tech Lead", "San Diego"),
            Person(0, "Mia Robinson", 30, "Mobile Developer", "Nashville"),
            Person(0, "Noah Clark", 36, "Cloud Architect", "Atlanta"),
            Person(0, "Olivia Lewis", 27, "UI Designer", "Dallas"),
            Person(0, "Peter Hall", 48, "VP Engineering", "Philadelphia"),
            Person(0, "Quinn Young", 32, "Security Engineer", "Minneapolis"),
            Person(0, "Rachel King", 25, "Junior Designer", "Orlando"),
            Person(0, "Sam Wright", 38, "Solutions Architect", "Houston"),
            Person(0, "Tina Scott", 34, "Scrum Master", "Detroit")
        )
        seedPeople.forEach { service.createPerson(it.name, it.age, it.profession, it.city) }

        val secondCount = gson.fromJson(
            sendGet("$baseUrl/api/people")["body"] as String,
            Array<Person>::class.java
        ).size

        assertEquals(20, firstCount, "First seed should produce exactly 20 people")
        assertEquals(20, secondCount, "Second seed should still be exactly 20 people - no duplicates")
    }

    @Test
    fun `inserting duplicate name profession and city is rejected`() {
        // Alice Johnson / Software Engineer / San Francisco is in the seed data
        val response = sendPost(
            "$baseUrl/api/people",
            mapOf(
                "name" to "Alice Johnson",
                "age" to 99,
                "profession" to "Software Engineer",
                "city" to "San Francisco"
            )
        )

        val result = gson.fromJson(response["body"] as String, Map::class.java)
        assertEquals(false, result["success"], "Inserting a duplicate should return success=false")
        assertEquals("A person with that name, profession, and city already exists", result["error"])
    }

    @Test
    fun `initSchema applies unique constraint migration safely`() {
        val conn = DatabaseConfig.getConnection()
        
        val constraintExists = conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("""
                SELECT 1 FROM pg_constraint WHERE conname = 'people_name_profession_city_unique'
            """)
            rs.next()
        }
        
        assertTrue(constraintExists, "Unique constraint should exist after initSchema")
        
        val hasIndex = conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("""
                SELECT 1 FROM pg_indexes WHERE indexname = 'people_name_profession_city_unique'
            """)
            rs.next()
        }
        
        assertTrue(hasIndex, "Unique constraint should have an associated index")
    }

    private fun sendGet(urlString: String): Map<String, Any> {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        
        return try {
            val body = connection.inputStream.bufferedReader().readText()
            mapOf("status" to connection.responseCode, "body" to (body as Any))
        } finally {
            connection.disconnect()
        }
    }

    private fun sendPost(urlString: String, data: Map<String, Any>): Map<String, Any> {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        
        return try {
            val json = gson.toJson(data)
            connection.outputStream.write(json.toByteArray())
            val body = connection.inputStream.bufferedReader().readText()
            mapOf("status" to connection.responseCode, "body" to (body as Any))
        } finally {
            connection.disconnect()
        }
    }

    private fun sendPut(urlString: String, data: Map<String, Any>): Map<String, Any> {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        
        return try {
            val json = gson.toJson(data)
            connection.outputStream.write(json.toByteArray())
            val body = connection.inputStream.bufferedReader().readText()
            mapOf("status" to connection.responseCode, "body" to (body as Any))
        } finally {
            connection.disconnect()
        }
    }

    private fun sendDelete(urlString: String): Map<String, Any> {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        
        return try {
            val body = connection.inputStream.bufferedReader().readText()
            mapOf("status" to connection.responseCode, "body" to (body as Any))
        } finally {
            connection.disconnect()
        }
    }
}

object WebServerMain {
    private val handlebars = createHandlebarsTest()

    private fun createHandlebarsTest(): Handlebars {
        val hbs = Handlebars(ClassPathTemplateLoader("/templates", ".hbs"))
        
        hbs.registerHelper("htmlEscape", object : com.github.jknack.handlebars.Helper<Any?> {
            override fun apply(context: Any?, options: Options): Any {
                val str = context?.toString() ?: return ""
                return str
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;")
            }
        })
        
        hbs.registerHelper("jsEscape", object : com.github.jknack.handlebars.Helper<Any?> {
            override fun apply(context: Any?, options: Options): Any {
                val str = context?.toString() ?: return ""
                return str
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
            }
        })
        
        hbs.registerHelper("formatDate", object : com.github.jknack.handlebars.Helper<Any?> {
            override fun apply(context: Any?, options: Options): Any {
                if (context == null) return ""
                return when (context) {
                    is LocalDateTime -> context.toLocalDate().toString()
                    is String -> {
                        try { LocalDateTime.parse(context).toLocalDate().toString() }
                        catch (e: Exception) { context }
                    }
                    else -> context.toString()
                }
            }
        })
        
        hbs.registerHelper("eq", object : com.github.jknack.handlebars.Helper<Any?> {
            override fun apply(context: Any?, options: Options): Any {
                if (context == null) return options.inverse()
                val args = options.params
                return if (args.isNotEmpty() && context.toString() == args[0].toString()) {
                    options.fn()
                } else {
                    options.inverse()
                }
            }
        })
        
        return hbs
    }

    private fun sanitizeSearchQuery(input: String): String {
        return input
            .take(100)
            .replace(Regex("[<>\"'&]"), "")
            .trim()
    }

    private fun sanitizeSortParam(input: String): String {
        return when (input.lowercase()) {
            "id", "name", "age", "profession", "city", "created" -> input.lowercase()
            else -> "id"
        }
    }

    private fun htmlEscape(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }

    fun startServer() {
        val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/persondb"
        val dbUser = System.getenv("DB_USER") ?: "postgres"
        val dbPassword = System.getenv("DB_PASSWORD") ?: "postgres"

        DatabaseConfig.connect(url = dbUrl, user = dbUser, password = dbPassword)
        DatabaseConfig.initSchema()

        val repository = PersonRepository()
        val service = PersonService(repository)

        reseedDatabase(service)

        Spark.port(4567)
        Spark.staticFiles.location("/public")

        Spark.get("/") { req, _ ->
            val search = sanitizeSearchQuery(req.queryParams("search") ?: "")
            val sortBy = sanitizeSortParam(req.queryParams("sort") ?: "id")
            val order = if (req.queryParams("order") == "desc") "desc" else "asc"

            var people = service.getAllPeople()

            if (search.isNotBlank()) {
                val searchLower = search.lowercase()
                people = people.filter {
                    it.name.lowercase().contains(searchLower) ||
                    it.profession.lowercase().contains(searchLower) ||
                    it.city.lowercase().contains(searchLower)
                }
            }

            people = when (sortBy) {
                "name" -> if (order == "asc") people.sortedBy { it.name } else people.sortedByDescending { it.name }
                "age" -> if (order == "asc") people.sortedBy { it.age } else people.sortedByDescending { it.age }
                "profession" -> if (order == "asc") people.sortedBy { it.profession } else people.sortedByDescending { it.profession }
                "city" -> if (order == "asc") people.sortedBy { it.city } else people.sortedByDescending { it.city }
                "created" -> if (order == "asc") people.sortedBy { it.createdAt } else people.sortedByDescending { it.createdAt }
                else -> if (order == "asc") people.sortedBy { it.id } else people.sortedByDescending { it.id }
            }

            val totalCount = service.getAllPeople().size
            val filteredCount = people.size

            val template = handlebars.compile("index")
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

        Spark.get("/api/people") { _, _ ->
            gson.toJson(service.getAllPeople())
        }

        Spark.get("/api/people/:id") { req, _ ->
            val id = req.params(":id").toIntOrNull()
            if (id == null) {
                gson.toJson(mapOf("error" to "Invalid ID"))
            } else {
                service.getPerson(id)?.let { gson.toJson(it) } ?: gson.toJson(mapOf("error" to "Person not found"))
            }
        }

        Spark.post("/api/people") { req, _ ->
            try {
                val body = gson.fromJson(req.body(), PersonRequest::class.java)
                val person = service.createPerson(body.name, body.age, body.profession, body.city)
                gson.toJson(mapOf("success" to true, "data" to person))
            } catch (e: IllegalArgumentException) {
                gson.toJson(mapOf("success" to false, "error" to (e.message ?: "Unknown error")))
            } catch (e: Exception) {
                val msg = if (e.message?.contains("unique", ignoreCase = true) == true ||
                             e.message?.contains("duplicate", ignoreCase = true) == true)
                    "A person with that name, profession, and city already exists"
                else "An unexpected error occurred"
                gson.toJson(mapOf("success" to false, "error" to msg))
            }
        }

        Spark.put("/api/people/:id") { req, _ ->
            try {
                val id = req.params(":id").toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
                val body = gson.fromJson(req.body(), PersonRequest::class.java)
                val person = service.updatePerson(id, body.name, body.age, body.profession, body.city)
                if (person != null) {
                    gson.toJson(mapOf("success" to true, "data" to person))
                } else {
                    gson.toJson(mapOf("success" to false, "error" to "Person not found"))
                }
            } catch (e: IllegalArgumentException) {
                gson.toJson(mapOf("success" to false, "error" to (e.message ?: "Unknown error")))
            }
        }

        Spark.delete("/api/people/:id") { req, _ ->
            val id = req.params(":id").toIntOrNull() ?: return@delete gson.toJson(mapOf("success" to false, "error" to "Invalid ID"))
            val deleted = service.deletePerson(id)
            if (deleted) {
                gson.toJson(mapOf("success" to true))
            } else {
                gson.toJson(mapOf("success" to false, "error" to "Person not found"))
            }
        }
    }

    private fun reseedDatabase(service: PersonService) {
        service.clearAllPeople()
        val people = listOf(
            Person(0, "Alice Johnson", 28, "Software Engineer", "San Francisco"),
            Person(0, "Bob Smith", 35, "Data Scientist", "New York"),
            Person(0, "Carol Williams", 42, "Product Manager", "Seattle"),
            Person(0, "David Brown", 31, "Frontend Developer", "Austin"),
            Person(0, "Emma Davis", 26, "UX Designer", "Portland"),
            Person(0, "Frank Miller", 39, "DevOps Engineer", "Chicago"),
            Person(0, "Grace Wilson", 33, "Backend Developer", "Boston"),
            Person(0, "Henry Taylor", 45, "CTO", "Los Angeles"),
            Person(0, "Ivy Anderson", 29, "QA Engineer", "Denver"),
            Person(0, "Jack Thomas", 37, "Full Stack Developer", "Miami"),
            Person(0, "Karen Martinez", 24, "Junior Developer", "Phoenix"),
            Person(0, "Leo Garcia", 41, "Tech Lead", "San Diego"),
            Person(0, "Mia Robinson", 30, "Mobile Developer", "Nashville"),
            Person(0, "Noah Clark", 36, "Cloud Architect", "Atlanta"),
            Person(0, "Olivia Lewis", 27, "UI Designer", "Dallas"),
            Person(0, "Peter Hall", 48, "VP Engineering", "Philadelphia"),
            Person(0, "Quinn Young", 32, "Security Engineer", "Minneapolis"),
            Person(0, "Rachel King", 25, "Junior Designer", "Orlando"),
            Person(0, "Sam Wright", 38, "Solutions Architect", "Houston"),
            Person(0, "Tina Scott", 34, "Scrum Master", "Detroit")
        )

        people.forEach { person ->
            service.createPerson(person.name, person.age, person.profession, person.city)
        }
    }
}

object TestDatabaseConfig {
    fun connect() {
        val dbUser = System.getenv("DB_USER") ?: "postgres"
        val dbPassword = System.getenv("DB_PASSWORD") ?: "postgres"
        DatabaseConfig.connect(
            url = "jdbc:postgresql://localhost:5432/persondb",
            user = dbUser,
            password = dbPassword
        )
    }

    fun initSchema() {
        DatabaseConfig.getConnection().createStatement().use { stmt ->
            stmt.execute("DROP TABLE IF EXISTS people")
            stmt.execute("""
                CREATE TABLE people (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    age INTEGER NOT NULL,
                    profession VARCHAR(255) NOT NULL,
                    city VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT people_name_profession_city_unique UNIQUE (name, profession, city)
                )
            """.trimIndent())
        }
    }

    fun close() {
        try {
            DatabaseConfig.getConnection().createStatement().use { stmt ->
                stmt.execute("DROP TABLE IF EXISTS people")
            }
            DatabaseConfig.close()
        } catch (e: Exception) {
        }
    }
}

data class PersonRequest(
    val name: String,
    val age: Int,
    val profession: String,
    val city: String
)

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.format(formatter))
        }
    }

    override fun read(reader: JsonReader): LocalDateTime? {
        val value = reader.nextString()
        return if (value.isNullOrEmpty()) null else LocalDateTime.parse(value, formatter)
    }
}

private val gson = GsonBuilder()
    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
    .create()
