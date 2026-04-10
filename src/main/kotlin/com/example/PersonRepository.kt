package com.example

import java.sql.ResultSet


interface PersonRepositoryInterface {
    fun save(person: Person): Person
    fun findById(id: Int): Person?
    fun findAll(): List<Person>
    fun update(id: Int, person: Person): Person?
    fun delete(id: Int): Boolean
    fun clearAll()
}

class PersonRepository : PersonRepositoryInterface {
    
    override fun save(person: Person): Person {
        val sql = "INSERT INTO people (name, age, profession, city) VALUES (?, ?, ?, ?) RETURNING id, created_at"
        
        DatabaseConfig.getConnection().prepareStatement(sql).use { stmt ->
            stmt.setString(1, person.name)
            stmt.setInt(2, person.age)
            stmt.setString(3, person.profession)
            stmt.setString(4, person.city)
            
            val rs = stmt.executeQuery()
            if (rs.next()) {
                val id = rs.getInt("id")
                val createdAt = rs.getTimestamp("created_at").toLocalDateTime()
                return person.copy(id = id, createdAt = createdAt)
            }
            return person
        }
    }

    override fun findById(id: Int): Person? {
        val sql = "SELECT id, name, age, profession, city, created_at FROM people WHERE id = ?"
        
        DatabaseConfig.getConnection().prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            val rs = stmt.executeQuery()
            
            if (rs.next()) {
                return rs.toPerson()
            }
            return null
        }
    }

    override fun findAll(): List<Person> {
        val sql = "SELECT id, name, age, profession, city, created_at FROM people"
        val people = mutableListOf<Person>()
        
        DatabaseConfig.getConnection().createStatement().use { stmt ->
            val rs = stmt.executeQuery(sql)
            
            while (rs.next()) {
                people.add(rs.toPerson())
            }
        }
        return people
    }

    override fun update(id: Int, person: Person): Person? {
        val sql = "UPDATE people SET name = ?, age = ?, profession = ?, city = ? WHERE id = ?"
        
        DatabaseConfig.getConnection().prepareStatement(sql).use { stmt ->
            stmt.setString(1, person.name)
            stmt.setInt(2, person.age)
            stmt.setString(3, person.profession)
            stmt.setString(4, person.city)
            stmt.setInt(5, id)
            
            val rowsUpdated = stmt.executeUpdate()
            return if (rowsUpdated > 0) findById(id) else null
        }
    }

    override fun delete(id: Int): Boolean {
        val sql = "DELETE FROM people WHERE id = ?"
        
        DatabaseConfig.getConnection().prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            return stmt.executeUpdate() > 0
        }
    }

    override fun clearAll() {
        DatabaseConfig.getConnection().createStatement().use { stmt ->
            stmt.execute("TRUNCATE TABLE people RESTART IDENTITY")
        }
    }

    private fun ResultSet.toPerson(): Person {
        val createdAt = getTimestamp("created_at")?.toLocalDateTime()
        return Person(
            id = getInt("id"),
            name = getString("name"),
            age = getInt("age"),
            profession = getString("profession"),
            city = getString("city"),
            createdAt = createdAt
        )
    }
}
