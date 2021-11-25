package com.example.listingapp.service

import com.example.listingapp.model.ResponseModel
import retrofit2.http.GET
import retrofit2.http.Query

interface ListingService {
    @GET("api/")
    suspend fun retrieveDetails(
        @Query("results") result: Int = 25
    ): ResponseModel

}

