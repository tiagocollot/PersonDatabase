package com.example

import java.sql.Connection
import java.sql.DriverManager

object DatabaseConfig {
    private var connection: Connection? = null

    fun connect(url: String, user: String, password: String) {
        connection = DriverManager.getConnection(url, user, password)
    }

    fun getConnection(): Connection {
        return connection ?: throw IllegalStateException("Database not initialized")
    }

    fun initSchema() {
        getConnection().createStatement().use { stmt ->
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS people (
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
        connection?.close()
        connection = null
    }
}
