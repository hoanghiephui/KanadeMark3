package caios.android.kanade.core.repository.podcast

import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import caios.android.kanade.core.model.podcast.LookFeedResponse
import com.podcast.core.network.datasource.ItunesDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface FeedDiscoveryRepository {
    suspend fun getTopPodcast(
        country: String,
        limit: Int
    ): ItunesTopPodcastResponse

    suspend fun getLookFeed(
        id: String
    ): LookFeedResponse
}

class FeedDiscoveryRepositoryImpl @Inject constructor(
    private val networkDataSource: ItunesDataSource,
    @Dispatcher(KanadeDispatcher.IO) private val dispatcher: CoroutineDispatcher,
) : FeedDiscoveryRepository {
    override suspend fun getTopPodcast(country: String, limit: Int): ItunesTopPodcastResponse =
        withContext(dispatcher) {
            networkDataSource.getTopPodcast(country, limit)
        }

    override suspend fun getLookFeed(id: String): LookFeedResponse =
        withContext(dispatcher) {
            networkDataSource.getLookFeed(id)
        }
}
