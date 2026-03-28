package com.example

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class PersonServiceTest {
    private lateinit var repository: MockPersonRepository
    private lateinit var service: PersonService

    @BeforeEach
    fun setup() {
        repository = MockPersonRepository()
        service = PersonService(repository)
    }

    @Test
    fun `createPerson saves valid person`() {
        val savedPerson = Person(1, "John", 30, "Dev", "NYC")
        repository.addPerson(savedPerson)

        val result = service.createPerson("John", 30, "Dev", "NYC")

        assertEquals("John", result.name)
        assertEquals(30, result.age)
    }

    @Test
    fun `createPerson throws when name is blank`() {
        assertThrows<IllegalArgumentException> {
            service.createPerson("  ", 30, "Dev", "NYC")
        }
    }

    @Test
    fun `createPerson throws when name is empty`() {
        assertThrows<IllegalArgumentException> {
            service.createPerson("", 30, "Dev", "NYC")
        }
    }

    @Test
    fun `createPerson throws when age is negative`() {
        assertThrows<IllegalArgumentException> {
            service.createPerson("John", -1, "Dev", "NYC")
        }
    }

    @Test
    fun `createPerson accepts age of zero`() {
        val savedPerson = Person(1, "Baby", 0, "None", "City")
        repository.addPerson(savedPerson)

        val result = service.createPerson("Baby", 0, "None", "City")

        assertEquals(0, result.age)
    }

    @Test
    fun `createPerson throws when age exceeds maximum`() {
        assertThrows<IllegalArgumentException> {
            service.createPerson("John", 151, "Dev", "NYC")
        }
    }

    @Test
    fun `createPerson throws when profession is blank`() {
        assertThrows<IllegalArgumentException> {
            service.createPerson("John", 30, "", "NYC")
        }
    }

    @Test
    fun `createPerson throws when profession is whitespace`() {
        assertThrows<IllegalArgumentException> {
            service.createPerson("John", 30, "   ", "NYC")
        }
    }

    @Test
    fun `createPerson throws when city is blank`() {
        assertThrows<IllegalArgumentException> {
            service.createPerson("John", 30, "Dev", "")
        }
    }

    @Test
    fun `createPerson throws when city is whitespace`() {
        assertThrows<IllegalArgumentException> {
            service.createPerson("John", 30, "Dev", "   ")
        }
    }

    @Test
    fun `createPerson trims whitespace`() {
        repository.addPerson(Person(1, "John", 30, "Dev", "NYC"))

        service.createPerson("  John  ", 30, "  Dev  ", "  NYC  ")

        val saved = repository.getLastSaved()
        assertEquals("John", saved?.name)
        assertEquals("Dev", saved?.profession)
        assertEquals("NYC", saved?.city)
    }

    @Test
    fun `getPerson returns person when exists`() {
        val person = Person(1, "John", 30, "Dev", "NYC")
        repository.addPerson(person)

        val result = service.getPerson(1)

        assertEquals(person, result)
    }

    @Test
    fun `getPerson returns null when not exists`() {
        val result = service.getPerson(9999)

        assertNull(result)
    }

    @Test
    fun `getAllPeople returns all persons`() {
        val people = listOf(
            Person(1, "John", 30, "Dev", "NYC"),
            Person(2, "Jane", 25, "Designer", "LA")
        )
        repository.setPeople(people)

        val result = service.getAllPeople()

        assertEquals(2, result.size)
        assertEquals(people, result)
    }

    @Test
    fun `getAllPeople returns empty list when no persons`() {
        repository.setPeople(emptyList())

        val result = service.getAllPeople()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `updatePerson updates when valid`() {
        val updated = Person(1, "John", 31, "Senior", "LA")
        repository.addPerson(Person(1, "John", 30, "Dev", "NYC"))
        repository.setUpdateResult(updated)

        val result = service.updatePerson(1, "John", 31, "Senior", "LA")

        assertEquals(updated, result)
    }

    @Test
    fun `updatePerson returns null when person not exists`() {
        repository.setUpdateResult(null)

        val result = service.updatePerson(9999, "John", 30, "Dev", "NYC")

        assertNull(result)
    }

    @Test
    fun `updatePerson throws when name is blank`() {
        assertThrows<IllegalArgumentException> {
            service.updatePerson(1, "", 30, "Dev", "NYC")
        }
    }

    @Test
    fun `updatePerson throws when age is negative`() {
        assertThrows<IllegalArgumentException> {
            service.updatePerson(1, "John", -1, "Dev", "NYC")
        }
    }

    @Test
    fun `updatePerson throws when profession is blank`() {
        assertThrows<IllegalArgumentException> {
            service.updatePerson(1, "John", 30, "", "NYC")
        }
    }

    @Test
    fun `updatePerson throws when city is blank`() {
        assertThrows<IllegalArgumentException> {
            service.updatePerson(1, "John", 30, "Dev", "")
        }
    }

    @Test
    fun `deletePerson returns true when person exists`() {
        repository.setDeleteResult(true)

        val result = service.deletePerson(1)

        assertTrue(result)
    }

    @Test
    fun `deletePerson returns false when person not exists`() {
        repository.setDeleteResult(false)

        val result = service.deletePerson(9999)

        assertFalse(result)
    }
}

class MockPersonRepository : PersonRepositoryInterface {
    private val storage = mutableMapOf<Int, Person>()
    private var nextId = 1
    private var deleteResult = false
    private var updateResult: Person? = null
    private var lastSaved: Person? = null

    fun addPerson(person: Person) {
        storage[person.id] = person
    }

    fun setPeople(people: List<Person>) {
        storage.clear()
        people.forEach { storage[it.id] = it }
    }

    fun setDeleteResult(result: Boolean) {
        deleteResult = result
    }

    fun setUpdateResult(result: Person?) {
        updateResult = result
    }

    fun getLastSaved(): Person? = lastSaved

    override fun save(person: Person): Person {
        val id = nextId++
        lastSaved = person.copy(id = id)
        storage[id] = lastSaved!!
        return lastSaved!!
    }

    override fun findById(id: Int): Person? = storage[id]

    override fun findAll(): List<Person> = storage.values.toList()

    override fun update(id: Int, person: Person): Person? {
        return updateResult
    }

    override fun delete(id: Int): Boolean = deleteResult
}
