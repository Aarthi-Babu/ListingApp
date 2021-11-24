package com.example.listingapp.api

import com.example.listingapp.model.ResponseModel
import com.example.listingapp.model.WeatherModel
import retrofit2.http.GET
import retrofit2.http.Query

interface ListingService {
    @GET("api/")
    suspend fun retrieveDetails(
        @Query("results") result: Int = 25
    ): ResponseModel

    @GET("weather")
    suspend fun getWeatherInfo(
        @Query("lat") lat: Double = 28.00,
        @Query("lon") lon: Double = 77.00,
        @Query("appid") apiKey: String = "38781e38750a335dd868104f722abf5d"
    ): WeatherModel
}

