package com.example.unit

import com.example.Person
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Options
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import java.time.LocalDateTime

class HandlebarsTest {
    
    private val handlebars = createHandlebars()

    private fun createHandlebars(): Handlebars {
        val template = Handlebars(ClassPathTemplateLoader("/templates", ".hbs"))
        
        template.registerHelper("htmlEscape", object : com.github.jknack.handlebars.Helper<Any?> {
            override fun apply(context: Any?, options: Options): Any {
                val str = context?.toString() ?: return ""
                return str
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;")
            }
        })
        
        template.registerHelper("jsEscape", object : com.github.jknack.handlebars.Helper<Any?> {
            override fun apply(context: Any?, options: Options): Any {
                val str = context?.toString() ?: return ""
                return str
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
            }
        })
        
        template.registerHelper("formatDate", object : com.github.jknack.handlebars.Helper<Any?> {
            override fun apply(context: Any?, options: Options): Any {
                if (context == null) return ""
                return when (context) {
                    is LocalDateTime -> context.toLocalDate().toString()
                    is String -> {
                        try { LocalDateTime.parse(context).toLocalDate().toString() }
                        catch (e: Exception) { context }
                    }
                    else -> context.toString()
                }
            }
        })
        
        template.registerHelper("eq", object : com.github.jknack.handlebars.Helper<Any?> {
            override fun apply(context: Any?, options: Options): Any {
                if (context == null) return options.inverse()
                val args = options.params
                return if (args.isNotEmpty() && context.toString() == args[0].toString()) {
                    options.fn()
                } else {
                    options.inverse()
                }
            }
        })
        
        return template
    }

    @Test
    fun `formatDate helper converts LocalDateTime to date`() {
        val template = handlebars.compileInline("{{formatDate datetime}}")
        
        val result = template.apply(mapOf("datetime" to LocalDateTime.of(2026, 3, 27, 14, 30, 0)))
        assertEquals("2026-03-27", result)
    }

    @Test
    fun `formatDate helper converts ISO datetime string to date`() {
        val template = handlebars.compileInline("{{formatDate datetime}}")
        
        val result = template.apply(mapOf("datetime" to "2026-03-27T14:30:00"))
        assertEquals("2026-03-27", result)
    }

    @Test
    fun `formatDate helper handles invalid datetime`() {
        val template = handlebars.compileInline("{{formatDate invalid}}")
        
        val result = template.apply(mapOf("invalid" to "not-a-date"))
        assertEquals("not-a-date", result)
    }

    @Test
    fun `formatDate helper handles non-string input`() {
        val template = handlebars.compileInline("{{formatDate number}}")
        assertEquals("42", template.apply(mapOf("number" to 42)))
    }

    @Test
    fun `template loads from classpath`() {
        val template = handlebars.compile("index")
        assertNotNull(template)
    }

    @Test
    fun `template renders HTML structure`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf(
            "people" to emptyList<Person>(),
            "search" to "",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 0,
            "filteredCount" to 0,
            "isFiltered" to false
        ))
        
        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("Person Database"))
        assertTrue(html.contains("Add New Person"))
    }

    @Test
    fun `template renders people with server-side data`() {
        val template = handlebars.compile("index")
        val people = listOf(
            Person(1, "John Doe", 30, "Developer", "NYC"),
            Person(2, "Jane Smith", 25, "Designer", "LA")
        )
        val html = template.apply(mapOf(
            "people" to people,
            "search" to "",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 2,
            "filteredCount" to 2,
            "isFiltered" to false
        ))
        
        assertTrue(html.contains("John Doe"))
        assertTrue(html.contains("30"))
        assertTrue(html.contains("Developer"))
        assertTrue(html.contains("NYC"))
        assertTrue(html.contains("Jane Smith"))
        assertTrue(html.contains("25"))
        assertTrue(html.contains("Designer"))
        assertTrue(html.contains("LA"))
    }

    @Test
    fun `template renders all required form fields`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf(
            "people" to emptyList<Person>(),
            "search" to "",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 0,
            "filteredCount" to 0,
            "isFiltered" to false
        ))
        
        assertTrue(html.contains("placeholder=\"Name\""))
        assertTrue(html.contains("placeholder=\"Age\""))
        assertTrue(html.contains("placeholder=\"Profession\""))
        assertTrue(html.contains("placeholder=\"City\""))
    }

    @Test
    fun `template includes Edit and Delete buttons`() {
        val template = handlebars.compile("index")
        val people = listOf(Person(1, "John", 30, "Dev", "NYC"))
        val html = template.apply(mapOf(
            "people" to people,
            "search" to "",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 1,
            "filteredCount" to 1,
            "isFiltered" to false
        ))
        
        assertTrue(html.contains("Edit"))
        assertTrue(html.contains("Delete"))
    }

    @Test
    fun `template includes table headers with sort links`() {
        val template = handlebars.compile("index")
        val people = listOf(Person(1, "John", 30, "Dev", "NYC"))
        val html = template.apply(mapOf(
            "people" to people,
            "search" to "",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 1,
            "filteredCount" to 1,
            "isFiltered" to false
        ))
        
        assertTrue(html.contains("<th>"))
        assertTrue(html.contains("ID</a></th>"))
        assertTrue(html.contains("Name</a></th>"))
        assertTrue(html.contains("Age</a></th>"))
        assertTrue(html.contains("Profession</a></th>"))
        assertTrue(html.contains("City</a></th>"))
        assertTrue(html.contains("Created</a></th>"))
        assertTrue(html.contains("<th>Actions</th>"))
    }

    @Test
    fun `template links to external stylesheet`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf(
            "people" to emptyList<Person>(),
            "search" to "",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 0,
            "filteredCount" to 0,
            "isFiltered" to false
        ))
        
        assertTrue(html.contains("<link"))
        assertTrue(html.contains("stylesheet"))
        assertTrue(html.contains("/styles.css"))
    }

    @Test
    fun `template includes search form`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf(
            "people" to emptyList<Person>(),
            "search" to "",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 0,
            "filteredCount" to 0,
            "isFiltered" to false
        ))
        
        assertTrue(html.contains("Search by name"))
        assertTrue(html.contains("name=\"search\""))
    }

    @Test
    fun `template shows empty state when no people`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf(
            "people" to emptyList<Person>(),
            "search" to "",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 0,
            "filteredCount" to 0,
            "isFiltered" to false
        ))
        
        assertTrue(html.contains("No people yet"))
    }

    @Test
    fun `template shows filtered message when searching`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf(
            "people" to emptyList<Person>(),
            "search" to "xyz",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 20,
            "filteredCount" to 0,
            "isFiltered" to true
        ))
        
        assertTrue(html.contains("No people found matching"))
        assertTrue(html.contains("xyz"))
    }

    @Test
    fun `template shows count when not filtered`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf(
            "people" to emptyList<Person>(),
            "search" to "",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 20,
            "filteredCount" to 20,
            "isFiltered" to false
        ))
        
        assertTrue(html.contains("20 people"))
    }

    @Test
    fun `template shows filtered count when filtered`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf(
            "people" to emptyList<Person>(),
            "search" to "dev",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 20,
            "filteredCount" to 5,
            "isFiltered" to true
        ))
        
        assertTrue(html.contains("Showing 5 of 20 people"))
    }

    @Test
    fun `template includes JavaScript for form handling`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf(
            "people" to emptyList<Person>(),
            "search" to "",
            "sortBy" to "id",
            "order" to "asc",
            "totalCount" to 0,
            "filteredCount" to 0,
            "isFiltered" to false
        ))
        
        assertTrue(html.contains("/api/people"))
        assertTrue(html.contains("addForm"))
        assertTrue(html.contains("editPerson"))
        assertTrue(html.contains("deletePerson"))
    }

    @Test
    fun `each helper renders multiple people`() {
        val template = handlebars.compileInline("{{#each people}}<p>{{name}}</p>{{/each}}")
        val people = listOf(
            Person(1, "John", 30, "Dev", "NYC"),
            Person(2, "Jane", 25, "Des", "LA")
        )
        val html = template.apply(mapOf("people" to people))
        
        assertTrue(html.contains("<p>John</p>"))
        assertTrue(html.contains("<p>Jane</p>"))
    }

    @Test
    fun `jsEscape helper escapes quotes and prevents XSS`() {
        val template = handlebars.compileInline("{{jsEscape text}}")
        
        val result = template.apply(mapOf("text" to "O'Brien"))
        assertFalse(result.contains("<script>"))
        assertFalse(result.contains("'"))
    }

    @Test
    fun `jsEscape helper escapes double quotes`() {
        val template = handlebars.compileInline("{{jsEscape text}}")
        
        val result = template.apply(mapOf("text" to "say \"hello\""))
        assertFalse(result.contains("\""))
        assertTrue(result.contains("\\\"") || result.contains("&quot;"))
    }

    @Test
    fun `jsEscape helper escapes newlines`() {
        val template = handlebars.compileInline("{{jsEscape text}}")
        
        val result = template.apply(mapOf("text" to "line1\nline2"))
        assertFalse(result.contains("\n"))
        assertTrue(result.contains("\\n"))
    }

    @Test
    fun `htmlEscape helper escapes special characters`() {
        val template = handlebars.compileInline("{{htmlEscape text}}")
        
        val result = template.apply(mapOf("text" to "<script>alert('xss')</script>"))
        assertFalse(result.contains("<script>"))
        assertFalse(result.contains(">"))
    }
}
