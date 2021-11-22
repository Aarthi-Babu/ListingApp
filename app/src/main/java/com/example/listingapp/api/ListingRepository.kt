package com.example.listingapp.api

import com.example.listingapp.model.ResponseModel
import com.example.listingapp.model.WeatherModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ListingRepository {

    private val baseUrl = "https://randomuser.me/"
    private var service: ListingService? = null
    private val weatherUrl = "https://api.openweathermap.org/data/2.5/"

    init {
        getRetrofitBuilder()
    }

    private fun getRetrofitBuilder(state: Int = 0) {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(if (state == 0) baseUrl else weatherUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        service = retrofit.create(ListingService::class.java)
    }

    suspend fun getDetails(): ResponseModel? {
        getRetrofitBuilder()
        return service?.retrieveDetails()
    }

    suspend fun getWeatherData(latitude: Int, longitude: Int): WeatherModel? {
        getRetrofitBuilder(1)
        return service?.getWeatherInfo(latitude, longitude)
    }
}