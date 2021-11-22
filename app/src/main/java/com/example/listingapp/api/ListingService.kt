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
        @Query("lat") lat: Int = 13,
        @Query("lon") lon: Int = 80,
        @Query("appid") apiKey: String = "38781e38750a335dd868104f722abf5d",
    ): WeatherModel
}

