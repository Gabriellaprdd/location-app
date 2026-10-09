package com.example.locationapp

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "locations")
@Parcelize
data class Location(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val address: String = "",
    val contact: String = "",
    val description: String = "",
    val openingHours: String = "",
    val visited: Boolean = false,
    val imageUri: String? = null
) : Parcelable