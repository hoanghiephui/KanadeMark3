package com.podcast.core.network.api

import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import retrofit2.http.GET
import retrofit2.http.Path

const val NAME_ITUNES = "itunes"

interface ItunesApi {
    @GET("{country}/rss/toppodcasts/limit={limit}/explicit=true/json")
    suspend fun getTopPodcast(
        @Path("country") country: String,
        @Path("limit") limit: Int
    ): ItunesTopPodcastResponse
}
