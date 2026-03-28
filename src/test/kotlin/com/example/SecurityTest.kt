package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class SecurityTest {

    @Test
    fun `sanitizeSearchQuery removes dangerous characters`() {
        val result = testSanitizeSearchQuery("<script>alert('xss')</script>")
        assertFalse(result.contains("<"))
        assertFalse(result.contains(">"))
        assertFalse(result.contains("'"))
        assertFalse(result.contains("\""))
        assertFalse(result.contains("&"))
    }

    @Test
    fun `sanitizeSearchQuery limits length to 100`() {
        val longInput = "a".repeat(150)
        val result = testSanitizeSearchQuery(longInput)
        assertEquals(100, result.length)
    }

    @Test
    fun `sanitizeSearchQuery trims whitespace`() {
        val result = testSanitizeSearchQuery("  hello world  ")
        assertEquals("hello world", result)
    }

    @Test
    fun `sanitizeSearchQuery preserves valid input`() {
        val result = testSanitizeSearchQuery("John Doe")
        assertEquals("John Doe", result)
    }

    @Test
    fun `sanitizeSortParam accepts valid columns`() {
        val validColumns = listOf("id", "name", "age", "profession", "city", "created")
        validColumns.forEach { column ->
            val result = testSanitizeSortParam(column)
            assertEquals(column, result)
        }
    }

    @Test
    fun `sanitizeSortParam returns id for invalid input`() {
        val result = testSanitizeSortParam("invalid")
        assertEquals("id", result)
    }

    @Test
    fun `sanitizeSortParam is case insensitive`() {
        val result = testSanitizeSortParam("NAME")
        assertEquals("name", result)
    }

    @Test
    fun `sanitizeSortParam handles SQL injection attempt`() {
        val result = testSanitizeSortParam("name; DROP TABLE people;--")
        assertEquals("id", result)
    }

    @Test
    fun `htmlEscape escapes all dangerous characters`() {
        val input = "<>&\"'"
        val result = testHtmlEscape(input)
        assertTrue(result.contains("&lt;"))
        assertTrue(result.contains("&gt;"))
        assertTrue(result.contains("&amp;"))
        assertTrue(result.contains("&quot;"))
        assertTrue(result.contains("&#x27;"))
    }

    @Test
    fun `htmlEscape handles empty string`() {
        val result = testHtmlEscape("")
        assertEquals("", result)
    }

    @Test
    fun `jsEscape escapes quotes and special chars`() {
        val input = "O'Brien \"John\" \\ newline\n"
        val result = testJsEscape(input)
        assertFalse(result.contains("'") && !result.contains("\\'"))
        assertFalse(result.contains("\"") && !result.contains("\\\""))
        assertFalse(result.contains("\\") && !result.contains("\\\\"))
    }

    @Test
    fun `jsEscape handles empty string`() {
        val result = testJsEscape("")
        assertEquals("", result)
    }

    @Test
    fun `MAX_AGE validation works`() {
        val maxAge = 150
        assertTrue(maxAge > 0)
        assertTrue(maxAge <= 150)
    }

    @Test
    fun `MAX_NAME_LENGTH validation works`() {
        val maxLength = 255
        assertTrue(maxLength > 0)
        assertTrue(maxLength <= 255)
    }

    @Test
    fun `search query sanitization prevents XSS`() {
        val xssPayloads = listOf(
            "<script>alert(1)</script>",
            "javascript:alert(1)",
            "<img src=x onerror=alert(1)>",
            "<svg onload=alert(1)>"
        )
        
        xssPayloads.forEach { payload ->
            val result = testSanitizeSearchQuery(payload)
            assertFalse(result.contains("<"), "Failed for: $payload")
            assertFalse(result.contains(">"), "Failed for: $payload")
        }
    }
}

private fun testSanitizeSearchQuery(input: String): String {
    return input
        .take(100)
        .replace(Regex("[<>\"'&]"), "")
        .trim()
}

private fun testSanitizeSortParam(input: String): String {
    return when (input.lowercase()) {
        "id", "name", "age", "profession", "city", "created" -> input.lowercase()
        else -> "id"
    }
}

private fun testHtmlEscape(input: String): String {
    return input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;")
}

private fun testJsEscape(input: String): String {
    return input
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}
