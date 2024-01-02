package com.podcast.core.network.datasource

import androidx.annotation.AnyThread
import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import caios.android.kanade.core.model.podcast.LookFeedResponse
import com.podcast.core.network.ApiServiceExecutor
import javax.inject.Inject

interface ItunesDataSource {
    @AnyThread
    suspend fun getTopPodcast(
        country: String,
        limit: Int
    ): ItunesTopPodcastResponse

    @AnyThread
    suspend fun getLookFeed(
        id: String
    ): LookFeedResponse

    @AnyThread
    suspend fun searchPodcast(
        query: String
    ): LookFeedResponse
}

class DefaultItunesDataSource @Inject constructor(
    private val executor: ApiServiceExecutor,
) : ItunesDataSource {

    override suspend fun getTopPodcast(country: String, limit: Int): ItunesTopPodcastResponse =
        executor.execute {
            it.getTopPodcast(country, limit)
        }

    override suspend fun getLookFeed(id: String): LookFeedResponse =
        executor.execute {
            it.getLookFeed(id)
        }

    override suspend fun searchPodcast(query: String): LookFeedResponse =
        executor.execute {
            it.searchPodcast(query = query)
        }
}
