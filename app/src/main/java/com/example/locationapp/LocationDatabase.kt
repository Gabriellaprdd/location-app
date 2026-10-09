package com.example.locationapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Location::class, Review::class], version = 2)
abstract class LocationDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var instance: LocationDatabase? = null

        fun getDatabase(context: Context): LocationDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LocationDatabase::class.java,
                    "location_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}