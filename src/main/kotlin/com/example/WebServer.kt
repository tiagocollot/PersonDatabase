package com.example

import spark.Spark.*
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

val gson = GsonBuilder()
    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
    .create()

fun main() {
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/persondb"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "postgres"

    DatabaseConfig.connect(url = dbUrl, user = dbUser, password = dbPassword)
    DatabaseConfig.initSchema()

    val repository = PersonRepository()
    val service = PersonService(repository)

    seedDatabaseIfEmpty(service)

    val handlebars = Handlebars(ClassPathTemplateLoader("/templates", ".hbs"))
        .registerHelper("escape") { context: Any, _: Any? ->
            context.toString()
        }
        .registerHelper("formatDate") { context: Any, _: Any? ->
            if (context is String) {
                try {
                    LocalDateTime.parse(context).toLocalDate().toString()
                } catch (e: Exception) {
                    context
                }
            } else context.toString()
        }

    port(4567)

    staticFiles.location("/public")

    get("/") { _, _ ->
        val people = service.getAllPeople()
        val template = handlebars.compile("index")
        template.apply(mapOf("people" to people))
    }

    get("/api/people") { _, _ ->
        val people = service.getAllPeople()
        gson.toJson(people)
    }

    get("/api/people/:id") { req, _ ->
        val id = req.params(":id").toIntOrNull()
        if (id == null) {
            gson.toJson(mapOf("error" to "Invalid ID"))
        } else {
            service.getPerson(id)?.let { gson.toJson(it) } ?: gson.toJson(mapOf("error" to "Person not found"))
        }
    }

    post("/api/people") { req, _ ->
        try {
            val body = gson.fromJson(req.body(), PersonRequest::class.java)
            val person = service.createPerson(body.name, body.age, body.profession, body.city)
            gson.toJson(mapOf("success" to true, "data" to person))
        } catch (e: IllegalArgumentException) {
            gson.toJson(mapOf("success" to false, "error" to (e.message ?: "Unknown error")))
        }
    }

    put("/api/people/:id") { req, _ ->
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

    delete("/api/people/:id") { req, _ ->
        val id = req.params(":id").toIntOrNull() ?: return@delete gson.toJson(mapOf("success" to false, "error" to "Invalid ID"))
        val deleted = service.deletePerson(id)
        if (deleted) {
            gson.toJson(mapOf("success" to true))
        } else {
            gson.toJson(mapOf("success" to false, "error" to "Person not found"))
        }
    }

    println("Server running at http://localhost:4567")
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

data class PersonRequest(
    val name: String,
    val age: Int,
    val profession: String,
    val city: String
)
