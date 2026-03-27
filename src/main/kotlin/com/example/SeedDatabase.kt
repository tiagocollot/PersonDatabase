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
        val saved = service.createPerson(person.name, person.age, person.profession, person.city)
        println("Created: $saved")
    }

    println("\nTotal people in database: ${service.getAllPeople().size}")

    DatabaseConfig.close()
}
