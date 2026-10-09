package com.example.locationapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewDaoTest {

    private lateinit var database: LocationDatabase
    private lateinit var reviewDao: ReviewDao

    @Before
    fun setup() {
        val context =
            ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            LocationDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        reviewDao = database.reviewDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertReview_reviewIsStored() = runBlocking {
        reviewDao.insert(
            Review(
                locationId = 1,
                userName = "Aisyah",
                comment = "Great place!",
                timestamp = 1000L,
                rating = 5
            )
        )

        val reviews =
            reviewDao.getAllReviews()
                .getOrAwaitValue()

        assertEquals(1, reviews.size)
        assertEquals("Aisyah", reviews.first().userName)
        assertEquals(5, reviews.first().rating)
    }

    @Test
    fun getReviewsForLocation_returnsCorrectReviews() = runBlocking {
        reviewDao.insert(
            Review(
                locationId = 1,
                userName = "Aisyah",
                comment = "Great",
                timestamp = 1000L,
                rating = 5
            )
        )

        reviewDao.insert(
            Review(
                locationId = 2,
                userName = "Daniel",
                comment = "Good",
                timestamp = 2000L,
                rating = 4
            )
        )

        val result =
            reviewDao.getReviewsForLocation(1)
                .getOrAwaitValue()

        assertEquals(1, result.size)
        assertEquals(1, result.first().locationId)
        assertEquals("Aisyah", result.first().userName)
    }

    @Test
    fun getReviewsForLocation_ordersNewestFirst() = runBlocking {
        reviewDao.insert(
            Review(
                locationId = 1,
                userName = "Older",
                comment = "Older review",
                timestamp = 1000L,
                rating = 3
            )
        )

        reviewDao.insert(
            Review(
                locationId = 1,
                userName = "Newest",
                comment = "Newest review",
                timestamp = 5000L,
                rating = 5
            )
        )

        val result =
            reviewDao.getReviewsForLocation(1)
                .getOrAwaitValue()

        assertEquals(2, result.size)
        assertEquals("Newest", result[0].userName)
        assertEquals("Older", result[1].userName)
    }

    @Test
    fun getAllReviews_returnsAllInsertedReviews() = runBlocking {
        reviewDao.insert(
            Review(
                locationId = 1,
                userName = "User One",
                comment = "Good",
                rating = 4
            )
        )

        reviewDao.insert(
            Review(
                locationId = 2,
                userName = "User Two",
                comment = "Excellent",
                rating = 5
            )
        )

        val result =
            reviewDao.getAllReviews()
                .getOrAwaitValue()

        assertEquals(2, result.size)
    }
}