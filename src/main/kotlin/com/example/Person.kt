package com.example

import java.time.LocalDateTime

data class Person(
    val id: Int = 0,
    val name: String,
    val age: Int,
    val profession: String,
    val city: String,
    val createdAt: LocalDateTime? = null
)
