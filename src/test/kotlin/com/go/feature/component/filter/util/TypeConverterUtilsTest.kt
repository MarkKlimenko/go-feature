package com.go.feature.component.filter.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TypeConverterUtilsTest {
    @Test
    fun versionRegexTest() {
        assertTrue(VERSION_REGEX.matches("0.1.2"))
        assertTrue(VERSION_REGEX.matches("1.1.2"))
        assertTrue(VERSION_REGEX.matches("0.0.1"))

        assertTrue(VERSION_REGEX.matches("0.0"))
        assertTrue(VERSION_REGEX.matches("0.1"))
        assertTrue(VERSION_REGEX.matches("3.1"))

        assertTrue(VERSION_REGEX.matches("3"))
        assertTrue(VERSION_REGEX.matches("0"))
    }

    @Test
    fun versionRegexErrorTest() {
        assertFalse(VERSION_REGEX.matches("12333.1.2"))
        assertFalse(VERSION_REGEX.matches(".1.2"))
        assertFalse(VERSION_REGEX.matches("0.0."))

        assertFalse(VERSION_REGEX.matches("0."))
        assertFalse(VERSION_REGEX.matches(".0"))
        assertFalse(VERSION_REGEX.matches("3.1.3.4"))

        assertFalse(VERSION_REGEX.matches(""))
    }

    @Test
    fun parseVersionTest() {
        assertEquals(100000000.0, parseVersion("", "1.0.0"))
        assertEquals(100000000.0, parseVersion("", "1"))

        assertEquals(100020003.0, parseVersion("", "1.2.3"))
        assertEquals(100220003.0, parseVersion("", "1.22.3"))
        assertEquals(100220003.0, parseVersion("", "1.22.03"))
        assertEquals(1100220033.0, parseVersion("", "11.22.33"))
    }
}