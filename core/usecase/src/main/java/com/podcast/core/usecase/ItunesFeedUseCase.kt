package com.podcast.core.usecase

import androidx.annotation.AnyThread
import caios.android.kanade.core.repository.podcast.FeedDiscoveryRepository
import com.podcast.core.network.api.Genres
import javax.inject.Inject

class ItunesFeedUseCase @Inject constructor(
    private val repository: FeedDiscoveryRepository,
) {
    @AnyThread
    suspend fun getTopPodcast(
        country: String,
        limit: Int
    ) = repository.getTopPodcast(country, limit)

    @AnyThread
    suspend fun getTopPodcastByGenres(
        country: String,
        limit: Int,
        genres: Genres
    ) = repository.getTopPodcastByGenres(country, limit, genres)

    /*@AnyThread
    suspend fun getLookFeed(id: String): Flow<FeedData> {
        withContext(ioDispatcher) {
            val lookFeed = async { repository.getLookFeed(id) }.await()
            if (lookFeed.results != null) {
                parseRssRepository.getRssDetail(lookFeed.results?.first()?.feedUrl!!)
            }

        }
    }*/
}
