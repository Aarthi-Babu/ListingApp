package com.example.listingapp.service

import com.example.listingapp.model.ResponseModel
import com.example.listingapp.model.WeatherModel
import com.example.listingapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ListingRepository @Inject constructor(
    private val listingService: ListingService,
    private val weatherService: WeatherService
) {


    suspend fun getDetails(): Resource<ResponseModel>? {
        return withContext(Dispatchers.IO) {
            try {
                Resource.success(listingService.retrieveDetails())
            } catch (e: Throwable) {
                Resource.error(e)
            }
        }

    }

    suspend fun getWeatherData(latitude: Double, longitude: Double): Resource<WeatherModel>? {
        return withContext(Dispatchers.IO) {
            try {
                Resource.success(weatherService.getWeatherInfo(latitude, longitude))
            } catch (e: Throwable) {
                Resource.error(e)
            }
        }
    }
}