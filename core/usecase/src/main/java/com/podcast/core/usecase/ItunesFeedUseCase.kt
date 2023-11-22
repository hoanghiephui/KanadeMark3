package com.podcast.core.usecase

import androidx.annotation.AnyThread
import caios.android.kanade.core.repository.podcast.FeedDiscoveryRepository
import javax.inject.Inject

class ItunesFeedUseCase @Inject constructor(private val repository: FeedDiscoveryRepository) {
    @AnyThread
    suspend fun getTopPodcast(
        country: String,
        limit: Int
    ) = repository.getTopPodcast(country, limit)
}