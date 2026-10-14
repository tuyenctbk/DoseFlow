package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApiService {

    @GET("drug/label.json")
    suspend fun searchDrugLabel(
        @Query("search", encoded = true) searchQuery: String,
        @Query("limit") limit: Int = 5
    ): OpenFdaLabelResponse
}
