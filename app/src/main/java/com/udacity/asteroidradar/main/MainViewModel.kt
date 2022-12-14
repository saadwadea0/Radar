package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.getNextSevenDaysFormattedDates
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    enum class NasaApiStatus { LOADING, DONE, ERROR }

    private val database = AsteroidsDatabase.getInstance(application)
    private val asteroidRepository = AsteroidRepository(database)

    private val startDate = getNextSevenDaysFormattedDates()[0]
    private val endDate = getNextSevenDaysFormattedDates()[5]
    private val weekEnd = getNextSevenDaysFormattedDates()[5]


    var asteroids: LiveData<List<Asteroid>> = asteroidRepository.asteroids

    private val _status = MutableLiveData<NasaApiStatus>()
    val status: LiveData<NasaApiStatus>
        get() = _status


    private val _pictureOfTheDay = MutableLiveData<PictureOfDay>()
    val pictureOfTheDay: LiveData<PictureOfDay>
        get() = _pictureOfTheDay


    init {
        getRefreshAsteroid()
    }

    private fun getRefreshAsteroid() {
        viewModelScope.launch {
            try {
                asteroidRepository.refreshAsteroid(startDate, endDate, API_KEY)
                getPictureOfTheDay()
            } catch (e: Exception) {

            }
        }
    }

    fun getWeekAsteroid(): LiveData<List<Asteroid>> {
        val weekAsteroid = Transformations.map(asteroidRepository.getWeekAsteroids(startDate, weekEnd)) {
            it.asDomainModel()
        }
        return weekAsteroid
    }

    fun getTodayAsteroids(): LiveData<List<Asteroid>> {
        val todayAsteroids = Transformations.map(asteroidRepository.getTodayAsteroids(startDate)) {
            it.asDomainModel()
        }
        return todayAsteroids
    }

    private fun getPictureOfTheDay() {
        viewModelScope.launch {
            try {
                _status.value = NasaApiStatus.LOADING
                val picture = asteroidRepository.getPictureOfTheDay(API_KEY)
                picture.let {
                    _pictureOfTheDay.value = picture
                    _status.value = NasaApiStatus.DONE
                }
            } catch (e: Exception) {
                _status.value = NasaApiStatus.ERROR
            }

        }
    }
}