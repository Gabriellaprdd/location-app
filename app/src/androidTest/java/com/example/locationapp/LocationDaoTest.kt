package com.example.locationapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationDaoTest {

    private lateinit var database: LocationDatabase
    private lateinit var locationDao: LocationDao

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

        locationDao = database.locationDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertLocation_locationIsStored() = runBlocking {
        locationDao.insertLocation(
            Location(
                name = "INTI International College Subang",
                category = "Campus",
                address = "Subang Jaya"
            )
        )

        val locations =
            locationDao.getAllLocations().getOrAwaitValue()

        assertEquals(1, locations.size)
        assertEquals(
            "INTI International College Subang",
            locations.first().name
        )
    }

    @Test
    fun getAllLocations_returnsAlphabeticalOrder() = runBlocking {
        locationDao.insertLocation(
            Location(
                name = "Sunway University",
                category = "Campus"
            )
        )

        locationDao.insertLocation(
            Location(
                name = "INTI International College Subang",
                category = "Campus"
            )
        )

        locationDao.insertLocation(
            Location(
                name = "Kaffe 16",
                category = "Cafe"
            )
        )

        val result =
            locationDao.getAllLocations().getOrAwaitValue()

        assertEquals(3, result.size)

        assertEquals(
            listOf(
                "INTI International College Subang",
                "Kaffe 16",
                "Sunway University"
            ),
            result.map { it.name }
        )
    }

    @Test
    fun updateLocation_updatesExistingRecord() = runBlocking {
        locationDao.insertLocation(
            Location(
                name = "LOKL Coffee Co",
                category = "Cafe"
            )
        )

        val original =
            locationDao.getAllLocations()
                .getOrAwaitValue()
                .first()

        val updated = original.copy(
            name = "LOKL Coffee Co Updated",
            visited = true
        )

        locationDao.updateLocation(updated)

        val result =
            locationDao.getById(original.id)
                .getOrAwaitValue()

        assertNotNull(result)
        assertEquals(
            "LOKL Coffee Co Updated",
            result?.name
        )
        assertTrue(result?.visited == true)
    }

    @Test
    fun visitedLocations_returnsOnlyVisitedLocations() = runBlocking {
        locationDao.insertLocation(
            Location(
                name = "Taman Eko Rimba KL",
                category = "Park",
                visited = true
            )
        )

        locationDao.insertLocation(
            Location(
                name = "Kaffe 16",
                category = "Cafe",
                visited = false
            )
        )

        val visited =
            locationDao.getVisitedLocations()
                .getOrAwaitValue()

        assertEquals(1, visited.size)
        assertEquals(
            "Taman Eko Rimba KL",
            visited.first().name
        )
        assertTrue(visited.first().visited)
    }

    @Test
    fun deleteLocation_removesLocation() = runBlocking {
        locationDao.insertLocation(
            Location(
                name = "Temporary Location",
                category = "Cafe"
            )
        )

        val location =
            locationDao.getAllLocations()
                .getOrAwaitValue()
                .first()

        locationDao.deleteLocation(location)

        val result =
            locationDao.getAllLocations()
                .getOrAwaitValue()

        assertTrue(result.isEmpty())
    }
}