package caios.android.kanade.core.repository.podcast

import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.database.podcast.PodcastDao
import caios.android.kanade.core.database.podcast.PodcastEntity
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import caios.android.kanade.core.model.podcast.LookFeedResponse
import caios.android.kanade.core.model.podcast.PodcastDownload
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

    suspend fun savePodcast(
        song: Song,
        filePath: String
    )

    suspend fun loadPodcast(podcastId: Long): PodcastDownload?
}

class FeedDiscoveryRepositoryImpl @Inject constructor(
    private val networkDataSource: ItunesDataSource,
    @Dispatcher(KanadeDispatcher.IO) private val dispatcher: CoroutineDispatcher,
    private val podcastDao: PodcastDao
) : FeedDiscoveryRepository {
    override suspend fun getTopPodcast(country: String, limit: Int): ItunesTopPodcastResponse =
        withContext(dispatcher) {
            networkDataSource.getTopPodcast(country, limit)
        }

    override suspend fun getLookFeed(id: String): LookFeedResponse =
        withContext(dispatcher) {
            networkDataSource.getLookFeed(id)
        }

    override suspend fun savePodcast(song: Song, filePath: String) = withContext(dispatcher) {
        val entity = PodcastEntity(
            podcastId = song.id,
            title = song.title,
            filePath = filePath,
            createdAt = System.currentTimeMillis()
        )
        podcastDao.insert(entity)
    }

    override suspend fun loadPodcast(podcastId: Long): PodcastDownload? = withContext(dispatcher) {
        podcastDao.getItemById(podcastId)?.toPodcast()
    }

    private fun PodcastEntity.toPodcast(): PodcastDownload {
        return PodcastDownload(
            podcastId = this.podcastId,
            title = this.title,
            filePath = this.filePath,
            createdAt = this.createdAt
        )
    }
}
