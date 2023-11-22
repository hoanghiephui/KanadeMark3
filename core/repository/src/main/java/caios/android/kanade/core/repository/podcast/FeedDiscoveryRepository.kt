package caios.android.kanade.core.repository.podcast

import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import com.podcast.core.network.NetworkDataSource
import javax.inject.Inject

interface FeedDiscoveryRepository {
    suspend fun getTopPodcast(
        country: String,
        limit: Int
    ): ItunesTopPodcastResponse
}

class FeedDiscoveryRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource
) : FeedDiscoveryRepository {
    override suspend fun getTopPodcast(country: String, limit: Int): ItunesTopPodcastResponse =
        networkDataSource.getTopPodcast(country, limit)
}
