package com.udacity.asteroidradar.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception

class AsteroidRepository(private val database: AsteroidsDatabase) {

    val asteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroid()) {
        it.asDomainModel()
    }

    fun getWeekAsteroids(startDate: String, endDate: String) = database.asteroidDao.getWeekAsteroids(startDate, endDate)

    fun getTodayAsteroids(todayDate: String) = database.asteroidDao.getTodayAsteroids(todayDate)

    fun deletePreviousDayAsteroid(date: String) = database.asteroidDao.deletePreviousDayAsteroids(date)

    suspend fun refreshAsteroid(startDate: String, endDate: String, apiKey: String) {
        withContext(Dispatchers.IO) {
            try {
                val resultList = AsteroidApi.retrofitService.getAsteroidProperties(startDate, endDate, apiKey)
                val asteroidList = parseAsteroidsJsonResult(JSONObject(resultList))
                database.asteroidDao.insertAll(*asteroidList.asDatabaseModel())
            } catch (e: Exception) {
                Timber.e(e.message!!)

            }
        }
    }

    suspend fun getPictureOfTheDay(apiKey: String) = PictureOfDayApi.retrofitService.getImageOfTheDay(apiKey)
}