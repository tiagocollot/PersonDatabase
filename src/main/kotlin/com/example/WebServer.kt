package com.example

import spark.Spark.*
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Options
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

val gson = GsonBuilder()
    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
    .create()

object RateLimiter {
    private val requests = ConcurrentHashMap<String, RateLimitEntry>()
    private const val MAX_REQUESTS = 100
    private const val WINDOW_MS = 60_000L

    data class RateLimitEntry(
        val count: AtomicInteger = AtomicInteger(1),
        val windowStart: Long = System.currentTimeMillis()
    )

    fun isAllowed(ip: String): Boolean {
        val now = System.currentTimeMillis()
        val entry = requests.compute(ip) { _, existing ->
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                RateLimitEntry(AtomicInteger(1), now)
            } else {
                existing.count.incrementAndGet()
                existing
            }
        }
        return entry!!.count.get() <= MAX_REQUESTS
    }

    fun getRemaining(ip: String): Int {
        val entry = requests[ip] ?: return MAX_REQUESTS
        return maxOf(0, MAX_REQUESTS - entry.count.get())
    }

    fun getResetTime(ip: String): Long {
        val entry = requests[ip] ?: return 0
        return maxOf(0, entry.windowStart + WINDOW_MS - System.currentTimeMillis())
    }
}

fun main() {
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/persondb"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "postgres"

    DatabaseConfig.connect(url = dbUrl, user = dbUser, password = dbPassword)
    DatabaseConfig.initSchema()

    val repository = PersonRepository()
    val service = PersonService(repository)

    seedDatabaseIfEmpty(service)

    val handlebars = createHandlebars()

    port(4567)
    staticFiles.location("/public")
    enableCORS()
    addSecurityHeaders()
    initRateLimiter()

    get("/") { req, _ ->
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
            "search" to htmlEscape(search),
            "sortBy" to sortBy,
            "order" to order,
            "totalCount" to totalCount,
            "filteredCount" to filteredCount,
            "isFiltered" to (search.isNotBlank() || sortBy != "id")
        ))
    }

    get("/api/people") { req, res ->
        res.header("X-RateLimit-Limit", "100")
        res.header("X-RateLimit-Remaining", RateLimiter.getRemaining(req.ip()).toString())
        res.header("X-RateLimit-Reset", (RateLimiter.getResetTime(req.ip()) / 1000).toString())
        gson.toJson(service.getAllPeople())
    }

    get("/api/people/:id") { req, res ->
        val id = req.params(":id").toIntOrNull()
        res.header("X-RateLimit-Remaining", RateLimiter.getRemaining(req.ip()).toString())
        when {
            id == null -> gson.toJson(mapOf("error" to "Invalid ID"))
            else -> service.getPerson(id)?.let { gson.toJson(it) } ?: gson.toJson(mapOf("error" to "Person not found"))
        }
    }

    post("/api/people") { req, res ->
        try {
            val body = gson.fromJson(req.body(), PersonRequest::class.java)
            val person = service.createPerson(body.name, body.age, body.profession, body.city)
            res.header("X-RateLimit-Remaining", RateLimiter.getRemaining(req.ip()).toString())
            gson.toJson(mapOf("success" to true, "data" to person))
        } catch (e: IllegalArgumentException) {
            res.header("X-RateLimit-Remaining", RateLimiter.getRemaining(req.ip()).toString())
            gson.toJson(mapOf("success" to false, "error" to (e.message ?: "Unknown error")))
        }
    }

    put("/api/people/:id") { req, res ->
        try {
            val id = req.params(":id").toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
            val body = gson.fromJson(req.body(), PersonRequest::class.java)
            val person = service.updatePerson(id, body.name, body.age, body.profession, body.city)
            res.header("X-RateLimit-Remaining", RateLimiter.getRemaining(req.ip()).toString())
            if (person != null) {
                gson.toJson(mapOf("success" to true, "data" to person))
            } else {
                gson.toJson(mapOf("success" to false, "error" to "Person not found"))
            }
        } catch (e: IllegalArgumentException) {
            res.header("X-RateLimit-Remaining", RateLimiter.getRemaining(req.ip()).toString())
            gson.toJson(mapOf("success" to false, "error" to (e.message ?: "Unknown error")))
        }
    }

    delete("/api/people/:id") { req, res ->
        val id = req.params(":id").toIntOrNull()
        res.header("X-RateLimit-Remaining", RateLimiter.getRemaining(req.ip()).toString())
        if (id == null) {
            return@delete gson.toJson(mapOf("success" to false, "error" to "Invalid ID"))
        }
        val deleted = service.deletePerson(id)
        if (deleted) {
            gson.toJson(mapOf("success" to true))
        } else {
            gson.toJson(mapOf("success" to false, "error" to "Person not found"))
        }
    }

    println("Server running at http://localhost:4567")
}

private fun enableCORS() {
    options("/*") { req, res ->
        val headers = req.headers("Access-Control-Request-Method")
        if (headers != null) {
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
            res.header("Access-Control-Max-Age", "86400")
        }
        res.header("Access-Control-Allow-Origin", "*")
        res.header("Vary", "Origin")
        res.status(204)
        res.body("")
    }

    before("/*") { _, res ->
        res.header("Access-Control-Allow-Origin", "*")
    }
}

private fun addSecurityHeaders() {
    after("/*") { _, res ->
        res.header("X-Frame-Options", "DENY")
        res.header("X-Content-Type-Options", "nosniff")
        res.header("X-XSS-Protection", "1; mode=block")
        res.header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
        res.header("Content-Security-Policy", "default-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self';")
        res.header("Referrer-Policy", "strict-origin-when-cross-origin")
        res.header("Permissions-Policy", "geolocation=(), microphone=(), camera=()")
        res.header("Cache-Control", "no-store, no-cache, must-revalidate")
        res.header("Pragma", "no-cache")
        res.header("Expires", "0")
    }

    get("/error") { _, res ->
        res.status(500)
        res.type("application/json")
        gson.toJson(mapOf("error" to "An unexpected error occurred. Please try again later."))
    }
}

private fun initRateLimiter() {
    before("/*") { req, _ ->
        val ip = req.ip()
        if (!RateLimiter.isAllowed(ip)) {
            halt(429, gson.toJson(mapOf(
                "error" to "Too many requests. Please try again later.",
                "retryAfter" to (RateLimiter.getResetTime(ip) / 1000)
            )))
        }
    }
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

private fun jsEscape(input: String): String {
    return input
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}

private fun createHandlebars(): Handlebars {
    val hbs = Handlebars(ClassPathTemplateLoader("/templates", ".hbs"))
    
    hbs.registerHelper("htmlEscape", object : com.github.jknack.handlebars.Helper<Any?> {
        override fun apply(context: Any?, options: Options): Any {
            val str = context?.toString() ?: return ""
            return htmlEscape(str)
        }
    })
    
    hbs.registerHelper("jsEscape", object : com.github.jknack.handlebars.Helper<Any?> {
        override fun apply(context: Any?, options: Options): Any {
            val str = context?.toString() ?: return ""
            return jsEscape(str)
        }
    })
    
    hbs.registerHelper("formatDate", object : com.github.jknack.handlebars.Helper<Any?> {
        override fun apply(context: Any?, options: Options): Any {
            if (context == null) return ""
            return when (context) {
                is LocalDateTime -> context.toLocalDate().toString()
                is String -> {
                    try {
                        LocalDateTime.parse(context).toLocalDate().toString()
                    } catch (e: Exception) {
                        context
                    }
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

private fun seedDatabaseIfEmpty(service: PersonService) {
    println("Seeding database with sample data...")
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
        println("Database seeded with ${people.size} people")
}

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

data class PersonRequest(
    val name: String,
    val age: Int,
    val profession: String,
    val city: String
)
