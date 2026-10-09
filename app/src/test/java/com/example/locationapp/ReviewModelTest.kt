package com.example.locationapp

import org.junit.Assert.*
import org.junit.Test

class ReviewModelTest {

    @Test
    fun review_storesCorrectValues() {
        val review = Review(
            id = 1,
            locationId = 5,
            userName = "Aisyah",
            comment = "Great place!",
            timestamp = 1000L,
            rating = 5
        )

        assertEquals(1, review.id)
        assertEquals(5, review.locationId)
        assertEquals("Aisyah", review.userName)
        assertEquals("Great place!", review.comment)
        assertEquals(1000L, review.timestamp)
        assertEquals(5, review.rating)
    }

    @Test
    fun review_copy_canChangeRating() {
        val review = Review(
            locationId = 1,
            userName = "Daniel",
            comment = "Good",
            rating = 3
        )

        val updated = review.copy(rating = 4)

        assertEquals(4, updated.rating)
        assertEquals("Daniel", updated.userName)
        assertEquals("Good", updated.comment)
    }
}