package caios.android.kanade.core.repository.podcast

import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.database.podcast.PodcastDao
import caios.android.kanade.core.database.podcast.PodcastEntity
import caios.android.kanade.core.database.podcast.PodcastFeedEntity
import caios.android.kanade.core.database.podcast.PodcastFeedItemEntity
import caios.android.kanade.core.database.podcast.PodcastModel
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import caios.android.kanade.core.model.podcast.LookFeedResponse
import caios.android.kanade.core.model.podcast.PodcastDownload
import com.podcast.core.network.datasource.ItunesDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
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

    suspend fun subscribePodcast(
        imId: String,
        artist: Artist
    )

    suspend fun onUnSubscribePodcast(imId: Long)

    suspend fun isSubscribe(idPodcast: Long): Boolean

    suspend fun loadItemSong(songId: Long): PodcastFeedItemEntity?
    suspend fun loadAddItem(): List<PodcastFeedItemEntity>

    suspend fun loadSubscribe(): List<PodcastModel>
    suspend fun loadLatestAdd(): List<PodcastModel>
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

    override suspend fun subscribePodcast(imId: String, artist: Artist): Unit =
        withContext(dispatcher) {
            val model = artist.toModel(imId)
            val idPodcast = podcastDao.insertPodcastFeed(model.podcastFeed)

            podcastDao.insertPodcastFeedItem(*model.items.map { it.copy(idPodcast = idPodcast) }
                .toTypedArray())
        }

    override suspend fun onUnSubscribePodcast(imId: Long) =
        withContext(dispatcher) {
            podcastDao.delete(imId)
        }

    override suspend fun isSubscribe(idPodcast: Long): Boolean =
        withContext(dispatcher) {
            podcastDao.load(idPodcast) != null
        }

    override suspend fun loadItemSong(songId: Long): PodcastFeedItemEntity? =
        withContext(dispatcher) {
            podcastDao.loadItemSong(songId)
        }

    override suspend fun loadAddItem(): List<PodcastFeedItemEntity> {
        return podcastDao.loadAddItem()
    }

    override suspend fun loadSubscribe(): List<PodcastModel> =
        withContext(dispatcher) {
            podcastDao.loadAll()
        }

    override suspend fun loadLatestAdd(): List<PodcastModel> =
        withContext(dispatcher) {
            podcastDao.loadLatestAdd()
        }

    private fun PodcastEntity.toPodcast(): PodcastDownload {
        return PodcastDownload(
            podcastId = this.podcastId,
            title = this.title,
            filePath = this.filePath,
            createdAt = this.createdAt
        )
    }

    private fun Artist.toModel(imId: String): PodcastModel {
        return PodcastModel().apply {
            podcastFeed = PodcastFeedEntity(
                id = artistId,
                imId = imId,
                idPodcast = artistId,
                title = artist,
                description = description,
                author = author,
                urlAvatar = urlAvatar,
                timeStamp = Clock.System.now().toEpochMilliseconds()
            )
            items = this@toModel.songs.map {
                PodcastFeedItemEntity(
                    idPodcast = artistId,
                    songId = it.id,
                    title = it.title,
                    artist = it.artist,
                    duration = it.duration,
                    year = it.year,
                    data = it.data,
                    dateModified = it.dateModified,
                    image = it.urlImage,
                    publishDate = it.publishDate
                )
            }
        }
    }
}
