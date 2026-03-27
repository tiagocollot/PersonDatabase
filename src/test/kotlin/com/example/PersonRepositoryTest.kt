package com.example

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class PersonRepositoryTest {
    private lateinit var repository: InMemoryPersonRepository

    @BeforeEach
    fun setup() {
        repository = InMemoryPersonRepository()
    }

    @Test
    fun `save inserts person and returns with id`() {
        val person = Person(0, "Alice", 25, "Engineer", "Boston")
        val result = repository.save(person)
        
        assertTrue(result.id > 0)
        assertEquals("Alice", result.name)
        assertEquals(25, result.age)
        assertEquals("Engineer", result.profession)
        assertEquals("Boston", result.city)
    }

    @Test
    fun `save generates unique ids`() {
        val person1 = repository.save(Person(0, "Alice", 25, "Engineer", "Boston"))
        val person2 = repository.save(Person(0, "Bob", 30, "Designer", "Seattle"))
        
        assertNotEquals(person1.id, person2.id)
    }

    @Test
    fun `save returns person with createdAt timestamp`() {
        val person = Person(0, "Alice", 25, "Engineer", "Boston")
        val result = repository.save(person)
        
        assertNotNull(result.createdAt)
    }

    @Test
    fun `findById returns person when exists`() {
        val saved = repository.save(Person(0, "Bob", 35, "Designer", "Seattle"))
        
        val found = repository.findById(saved.id)
        
        assertNotNull(found)
        assertEquals("Bob", found!!.name)
        assertEquals(35, found.age)
    }

    @Test
    fun `findById returns person with createdAt`() {
        val saved = repository.save(Person(0, "Bob", 35, "Designer", "Seattle"))
        
        val found = repository.findById(saved.id)
        
        assertNotNull(found!!.createdAt)
    }

    @Test
    fun `findById returns null when not exists`() {
        val found = repository.findById(9999)
        assertNull(found)
    }

    @Test
    fun `findAll returns all persons`() {
        repository.save(Person(0, "Person1", 20, "Job1", "City1"))
        repository.save(Person(0, "Person2", 30, "Job2", "City2"))
        
        val all = repository.findAll()
        
        assertEquals(2, all.size)
    }

    @Test
    fun `findAll returns all persons with createdAt`() {
        repository.save(Person(0, "Person1", 20, "Job1", "City1"))
        repository.save(Person(0, "Person2", 30, "Job2", "City2"))
        
        val all = repository.findAll()
        
        all.forEach { person ->
            assertNotNull(person.createdAt)
        }
    }

    @Test
    fun `findAll returns empty list when no persons`() {
        val all = repository.findAll()
        
        assertTrue(all.isEmpty())
    }

    @Test
    fun `update modifies existing person`() {
        val saved = repository.save(Person(0, "Original", 25, "OldJob", "OldCity"))
        
        val updated = repository.update(saved.id, Person(saved.id, "Updated", 26, "NewJob", "NewCity"))
        
        assertNotNull(updated)
        assertEquals("Updated", updated!!.name)
        assertEquals(26, updated.age)
        assertEquals("NewJob", updated.profession)
        assertEquals("NewCity", updated.city)
    }

    @Test
    fun `update preserves original createdAt`() {
        val saved = repository.save(Person(0, "Original", 25, "OldJob", "OldCity"))
        val originalCreatedAt = saved.createdAt
        
        val updated = repository.update(saved.id, Person(saved.id, "Updated", 26, "NewJob", "NewCity"))
        
        assertEquals(originalCreatedAt, updated!!.createdAt)
    }

    @Test
    fun `update returns null when person not exists`() {
        val updated = repository.update(9999, Person(9999, "Updated", 26, "Job", "City"))
        
        assertNull(updated)
    }

    @Test
    fun `delete removes person and returns true`() {
        val saved = repository.save(Person(0, "ToDelete", 25, "Job", "City"))
        
        val result = repository.delete(saved.id)
        
        assertTrue(result)
        assertNull(repository.findById(saved.id))
    }

    @Test
    fun `delete returns false when person not exists`() {
        val result = repository.delete(9999)
        assertFalse(result)
    }
}

class InMemoryPersonRepository : PersonRepositoryInterface {
    private val storage = mutableMapOf<Int, Person>()
    private var nextId = 1

    override fun save(person: Person): Person {
        val id = nextId++
        val now = java.time.LocalDateTime.now()
        val saved = person.copy(id = id, createdAt = now)
        storage[id] = saved
        return saved
    }

    override fun findById(id: Int): Person? = storage[id]

    override fun findAll(): List<Person> = storage.values.toList()

    override fun update(id: Int, person: Person): Person? {
        if (storage.containsKey(id)) {
            val existing = storage[id]!!
            val updated = person.copy(id = id, createdAt = existing.createdAt)
            storage[id] = updated
            return updated
        }
        return null
    }

    override fun delete(id: Int): Boolean = storage.remove(id) != null
}
