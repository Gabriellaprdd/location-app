package com.example.locationapp

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): LiveData<List<Location>>

    @Query("SELECT * FROM locations WHERE visited = 1")
    fun getVisitedLocations(): LiveData<List<Location>>

    @Query("SELECT * FROM locations WHERE id = :id")
    fun getById(id: Int): LiveData<Location?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location)

    @Update
    suspend fun updateLocation(location: Location)

    @Delete
    suspend fun deleteLocation(location: Location)
}