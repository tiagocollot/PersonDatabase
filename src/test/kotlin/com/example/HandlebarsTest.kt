package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import java.time.LocalDateTime

class HandlebarsTest {
    
    private val handlebars = Handlebars(ClassPathTemplateLoader("/templates", ".hbs"))
        .registerHelper("escape") { context: Any, _: Any? ->
            context.toString()
        }
        .registerHelper("formatDate") { context: Any, _: Any? ->
            if (context is String) {
                try {
                    LocalDateTime.parse(context).toLocalDate().toString()
                } catch (e: Exception) {
                    context
                }
            } else context.toString()
        }

    @Test
    fun `formatDate helper converts ISO datetime to date`() {
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
        val html = template.apply(mapOf("people" to emptyList<Person>()))
        
        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("Person Database"))
        assertTrue(html.contains("Add New Person"))
    }

    @Test
    fun `template renders all required form fields`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf("people" to emptyList<Person>()))
        
        assertTrue(html.contains("placeholder=\"Name\""))
        assertTrue(html.contains("placeholder=\"Age\""))
        assertTrue(html.contains("placeholder=\"Profession\""))
        assertTrue(html.contains("placeholder=\"City\""))
    }

    @Test
    fun `template includes Edit and Delete buttons`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf("people" to emptyList<Person>()))
        
        assertTrue(html.contains("Edit"))
        assertTrue(html.contains("Delete"))
    }

    @Test
    fun `template includes table headers`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf("people" to emptyList<Person>()))
        
        assertTrue(html.contains("<th>ID</th>"))
        assertTrue(html.contains("<th>Name</th>"))
        assertTrue(html.contains("<th>Age</th>"))
        assertTrue(html.contains("<th>Profession</th>"))
        assertTrue(html.contains("<th>City</th>"))
        assertTrue(html.contains("<th>Created</th>"))
        assertTrue(html.contains("<th>Actions</th>"))
    }

    @Test
    fun `template links to external stylesheet`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf("people" to emptyList<Person>()))
        
        assertTrue(html.contains("<link"))
        assertTrue(html.contains("stylesheet"))
        assertTrue(html.contains("/styles.css"))
    }

    @Test
    fun `template includes JavaScript for API calls`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf("people" to emptyList<Person>()))
        
        assertTrue(html.contains("/api/people"))
        assertTrue(html.contains("loadPeople"))
        assertTrue(html.contains("addForm"))
        assertTrue(html.contains("editPerson"))
        assertTrue(html.contains("deletePerson"))
    }

    @Test
    fun `template has empty table body for client-side rendering`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf("people" to emptyList<Person>()))
        
        assertTrue(html.contains("id=\"peopleTable\""))
        assertTrue(html.contains("<tbody id=\"peopleTable\"></tbody>"))
    }

    @Test
    fun `template includes add form handler`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf("people" to emptyList<Person>()))
        
        assertTrue(html.contains("addForm"))
        assertTrue(html.contains("addEventListener"))
    }

    @Test
    fun `template includes escapeHtml function for client-side sanitization`() {
        val template = handlebars.compile("index")
        val html = template.apply(mapOf("people" to emptyList<Person>()))
        
        assertTrue(html.contains("escapeHtml"))
        assertTrue(html.contains("textContent"))
    }
}
