package caios.android.kanade.core.repository.podcast

import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.database.podcast.PodcastDao
import caios.android.kanade.core.database.podcast.PodcastFeedItemEntity
import caios.android.kanade.core.database.podcast.PodcastModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface UpdatePodcastRepository {
    suspend fun loadAddPodcast(): List<PodcastModel>

    suspend fun deleteEpisodes(podcastIds: List<Long>)

    suspend fun insertPodcastFeedItem(entity: List<PodcastFeedItemEntity>)
}

class UpdatePodcastRepositoryImpl @Inject constructor(
    @Dispatcher(KanadeDispatcher.IO) private val dispatcher: CoroutineDispatcher,
    private val podcastDao: PodcastDao
) : UpdatePodcastRepository {
    override suspend fun loadAddPodcast(): List<PodcastModel> = withContext(dispatcher) {
        podcastDao.loadPodcastAll()
    }

    override suspend fun deleteEpisodes(podcastIds: List<Long>) = withContext(dispatcher) {
        podcastDao.deleteEpisodes(podcastIds)
    }

    override suspend fun insertPodcastFeedItem(entity: List<PodcastFeedItemEntity>) = withContext(dispatcher) {
        podcastDao.insertPodcastFeedItem(entity)
    }
}
