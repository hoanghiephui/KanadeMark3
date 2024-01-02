package com.podcast.core.network.api

import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import caios.android.kanade.core.model.podcast.LookFeedResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

const val NAME_ITUNES = "itunes"

interface ItunesApi {
    @GET("{country}/rss/toppodcasts/limit={limit}/explicit=true/json")
    suspend fun getTopPodcast(
        @Path("country") country: String,
        @Path("limit") limit: Int
    ): ItunesTopPodcastResponse

    @GET("lookup")
    suspend fun getLookFeed(
        @Query("id") id: String
    ): LookFeedResponse

    @GET("search")
    suspend fun searchPodcast(
        @Query("media") media: String = "podcast",
        @Query("term") query: String
    ): LookFeedResponse
}
