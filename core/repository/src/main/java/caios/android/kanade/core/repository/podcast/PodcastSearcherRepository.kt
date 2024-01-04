package caios.android.kanade.core.repository.podcast

import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.model.podcast.LookFeedResponse
import com.podcast.core.network.datasource.ItunesDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface PodcastSearcherRepository {
    suspend fun searchPodcast(query: String): LookFeedResponse
}

class ItunesSearcherRepositoryImpl @Inject constructor(
    @Dispatcher(KanadeDispatcher.IO)
    private val dispatcher: CoroutineDispatcher,
    private val networkDataSource: ItunesDataSource,
) : PodcastSearcherRepository {

    override suspend fun searchPodcast(query: String): LookFeedResponse =
        withContext(dispatcher) {
            networkDataSource.searchPodcast(query)
        }
}
