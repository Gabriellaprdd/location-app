package com.example.locationapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val locationId: Int,
    val userName: String,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis(),
    val rating: Int
)