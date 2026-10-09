package com.example.locationapp

import org.junit.Assert.*
import org.junit.Test

class LocationModelTest {

    @Test
    fun location_defaultValues_areCorrect() {
        val location = Location(
            name = "INTI",
            category = "Campus"
        )

        assertEquals(0, location.id)
        assertEquals("INTI", location.name)
        assertEquals("Campus", location.category)
        assertEquals("", location.address)
        assertEquals("", location.contact)
        assertEquals("", location.description)
        assertEquals("", location.openingHours)
        assertFalse(location.visited)
        assertNull(location.imageUri)
    }

    @Test
    fun location_copy_canChangeVisitedStatus() {
        val location = Location(
            id = 1,
            name = "Sunway University",
            category = "Campus",
            visited = false
        )

        val updated = location.copy(visited = true)

        assertTrue(updated.visited)
        assertEquals(location.id, updated.id)
        assertEquals(location.name, updated.name)
        assertEquals(location.category, updated.category)
    }

    @Test
    fun location_copy_canUpdateDetails() {
        val location = Location(
            id = 1,
            name = "Old Name",
            category = "Cafe"
        )

        val updated = location.copy(
            name = "New Name",
            address = "Kuala Lumpur"
        )

        assertEquals("New Name", updated.name)
        assertEquals("Kuala Lumpur", updated.address)
        assertEquals(1, updated.id)
    }
}