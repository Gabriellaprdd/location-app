package com.example.locationapp

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReviewDao {

    @Insert
    suspend fun insert(review: Review)

    @Query("SELECT * FROM reviews WHERE locationId = :locationId ORDER BY timestamp DESC")
    fun getReviewsForLocation(locationId: Int): LiveData<List<Review>>

    @Query("SELECT * FROM reviews")
    fun getAllReviews(): LiveData<List<Review>>
}