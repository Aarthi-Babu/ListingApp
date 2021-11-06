package com.example.listingapp.api

import com.example.listingapp.model.ResponseModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ListingRepository {

    private val baseUrl = "https://randomuser.me/"
    private val service: ListingService

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        service = retrofit.create(ListingService::class.java)
    }

    suspend fun getDetails(): ResponseModel {
        return service.retrieveDetails()
    }

}