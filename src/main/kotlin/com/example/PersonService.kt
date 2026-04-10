package com.example

class PersonService(private val repository: PersonRepositoryInterface) {

    companion object {
        const val MAX_NAME_LENGTH = 255
        const val MAX_PROFESSION_LENGTH = 255
        const val MAX_CITY_LENGTH = 255
        const val MAX_AGE = 150
    }

    fun createPerson(name: String, age: Int, profession: String, city: String): Person {
        validateInput(name, age, profession, city)
        
        val person = Person(
            name = name.trim().take(MAX_NAME_LENGTH),
            age = age,
            profession = profession.trim().take(MAX_PROFESSION_LENGTH),
            city = city.trim().take(MAX_CITY_LENGTH)
        )
        return repository.save(person)
    }

    fun getPerson(id: Int): Person? = repository.findById(id)

    fun getAllPeople(): List<Person> = repository.findAll()

    fun updatePerson(id: Int, name: String, age: Int, profession: String, city: String): Person? {
        validateInput(name, age, profession, city)
        
        return repository.update(id, Person(
            id = id,
            name = name.trim().take(MAX_NAME_LENGTH),
            age = age,
            profession = profession.trim().take(MAX_PROFESSION_LENGTH),
            city = city.trim().take(MAX_CITY_LENGTH)
        ))
    }

    fun deletePerson(id: Int): Boolean = repository.delete(id)

    fun clearAllPeople() = repository.clearAll()

    private fun validateInput(name: String, age: Int, profession: String, city: String) {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(age >= 0) { "Age cannot be negative" }
        require(age <= MAX_AGE) { "Age cannot exceed $MAX_AGE" }
        require(profession.isNotBlank()) { "Profession cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
    }
}
