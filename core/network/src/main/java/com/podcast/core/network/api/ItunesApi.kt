package com.podcast.core.network.api

import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import caios.android.kanade.core.model.podcast.LookFeedResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ItunesApi {
    @GET("{country}/rss/toppodcasts/limit={limit}/explicit=true/json")
    suspend fun getTopPodcast(
        @Path("country") country: String,
        @Path("limit") limit: Int
    ): ItunesTopPodcastResponse

    @GET("{country}/rss/toppodcasts/limit={limit}/genre={genre}/explicit=true/json")
    suspend fun getTopPodcastByGenres(
        @Path("country") country: String,
        @Path("limit") limit: Int,
        @Path("genre") genre: Int
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


enum class Genres(val id: Int) {
    TOP(0),
    HEALTH(1512),
    EDUCATION(1304),
    MUSIC(1310),
    SOCIETY(1324);//Society & Culture
    companion object {
        fun fromInt(value: Int) = entries.first { it.id == value }
    }
}
