package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface RxNavApiService {

    @GET("REST/spellingsuggestions.json")
    suspend fun getSpellingSuggestions(
        @Query("name") name: String
    ): RxNavSpellingResponse

    @GET("REST/approximateTerm.json")
    suspend fun getApproximateTerms(
        @Query("term") term: String,
        @Query("maxEntries") maxEntries: Int = 15
    ): RxNavApproximateResponse

    @GET("REST/drugs.json")
    suspend fun getDrugsByName(
        @Query("name") name: String
    ): RxNavDrugsResponse

    @GET("REST/rxcui.json")
    suspend fun getRxcuiByName(
        @Query("name") name: String
    ): RxNavRxcuiResponse

    @GET("REST/interaction/list.json")
    suspend fun getInteractionList(
        @Query("rxcuis") rxcuis: String
    ): RxNavInteractionListResponse
}
