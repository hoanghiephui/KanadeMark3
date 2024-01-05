package com.podcast.core.network.api

import caios.android.kanade.core.model.podcast.FyydResponse
import caios.android.kanade.core.model.podcast.IndexResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface IndexApi {
    @GET("search/byterm")
    suspend fun searchPodcasts(
        @Query("q") query: String,
        @Query("max") max: Int = 20,
    ): IndexResponse
}
