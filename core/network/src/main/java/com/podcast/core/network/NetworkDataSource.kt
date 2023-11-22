package com.podcast.core.network

import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse

interface NetworkDataSource {
    suspend fun getTopPodcast(
        country: String,
        limit: Int
    ): ItunesTopPodcastResponse
}
