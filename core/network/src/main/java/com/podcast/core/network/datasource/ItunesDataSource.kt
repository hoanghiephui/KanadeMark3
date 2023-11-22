package com.podcast.core.network.datasource

import androidx.annotation.AnyThread
import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import com.podcast.core.network.ApiServiceExecutor
import javax.inject.Inject

interface ItunesDataSource {
    @AnyThread
    suspend fun getTopPodcast(
        country: String,
        limit: Int
    ): ItunesTopPodcastResponse
}

class DefaultItunesDataSource @Inject constructor(
    private val executor: ApiServiceExecutor,
) : ItunesDataSource {

    override suspend fun getTopPodcast(country: String, limit: Int): ItunesTopPodcastResponse =
        executor.execute {
            it.getTopPodcast(country, limit)
        }
}