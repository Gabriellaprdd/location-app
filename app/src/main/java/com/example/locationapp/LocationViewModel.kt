package com.example.locationapp

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val locationDao = LocationDatabase.getDatabase(application).locationDao()
    private val reviewDao = LocationDatabase.getDatabase(application).reviewDao()

    val allLocations: LiveData<List<Location>> = locationDao.getAllLocations()
    val visitedLocations: LiveData<List<Location>> = locationDao.getVisitedLocations()
    val allReviews: LiveData<List<Review>> = reviewDao.getAllReviews()

    fun insert(location: Location) = viewModelScope.launch {
        locationDao.insertLocation(location)
    }

    fun update(location: Location) = viewModelScope.launch {
        locationDao.updateLocation(location)
    }

    fun delete(location: Location) = viewModelScope.launch {
        locationDao.deleteLocation(location)
    }

    fun insertReview(review: Review) = viewModelScope.launch {
        reviewDao.insert(review)
    }

    fun getReviewsForLocation(locationId: Int): LiveData<List<Review>> {
        return reviewDao.getReviewsForLocation(locationId)
    }

    fun getLocationById(id: Int): LiveData<Location?> {
        return locationDao.getById(id)
    }

    fun getLocationByIdSync(id: Int): Location? {
        return allLocations.value?.find { it.id == id }
    }
}
