package com.podcast.core.network.datasource

import androidx.annotation.AnyThread
import caios.android.kanade.core.model.podcast.FyydResponse
import com.podcast.core.network.ApiServiceExecutor
import javax.inject.Inject

interface FyyDDataSource {
    @AnyThread
    suspend fun searchPodcast(
        query: String,
        limit: Int = 10
    ): FyydResponse
}

class DefaultFyyDDataSource @Inject constructor(
    private val executor: ApiServiceExecutor,
) : FyyDDataSource {
    override suspend fun searchPodcast(query: String, limit: Int): FyydResponse =
        executor.executeFyyD {
            it.searchPodcasts(query, limit)
        }

}
