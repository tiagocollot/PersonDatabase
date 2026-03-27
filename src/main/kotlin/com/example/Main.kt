package com.example

fun main() {
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/persondb"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "postgres"

    DatabaseConfig.connect(
        url = dbUrl,
        user = dbUser,
        password = dbPassword
    )
    DatabaseConfig.initSchema()

    val repository = PersonRepository()
    val service = PersonService(repository)

    println("Creating person...")
    val person = service.createPerson("John Doe", 30, "Developer", "New York")
    println("Created: $person")

    println("\nGetting person ${person.id}...")
    println("Found: ${service.getPerson(person.id)}")

    println("\nUpdating person ${person.id}...")
    val updated = service.updatePerson(person.id, "John Doe", 31, "Senior Developer", "San Francisco")
    println("Updated: $updated")

    println("\nAll people: ${service.getAllPeople()}")

    println("\nDeleting person ${person.id}...")
    service.deletePerson(person.id)
    println("Deleted. All people: ${service.getAllPeople()}")

    DatabaseConfig.close()
}
