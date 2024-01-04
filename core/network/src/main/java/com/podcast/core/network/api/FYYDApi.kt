package com.podcast.core.network.api

import caios.android.kanade.core.model.podcast.FyydResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FYYDApi {
    @GET("search/podcast")
    suspend fun searchPodcasts(
        @Query("term") term: String,
        @Query("count") limit: Int?
    ): FyydResponse
}
