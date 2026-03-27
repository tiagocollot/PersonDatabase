package com.example

class PersonService(private val repository: PersonRepositoryInterface) {

    fun createPerson(name: String, age: Int, profession: String, city: String): Person {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(age >= 0) { "Age cannot be negative" }
        require(profession.isNotBlank()) { "Profession cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        
        val person = Person(
            name = name.trim(),
            age = age,
            profession = profession.trim(),
            city = city.trim()
        )
        return repository.save(person)
    }

    fun getPerson(id: Int): Person? = repository.findById(id)

    fun getAllPeople(): List<Person> = repository.findAll()

    fun updatePerson(id: Int, name: String, age: Int, profession: String, city: String): Person? {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(age >= 0) { "Age cannot be negative" }
        require(profession.isNotBlank()) { "Profession cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        
        return repository.update(id, Person(
            id = id,
            name = name.trim(),
            age = age,
            profession = profession.trim(),
            city = city.trim()
        ))
    }

    fun deletePerson(id: Int): Boolean = repository.delete(id)
}
