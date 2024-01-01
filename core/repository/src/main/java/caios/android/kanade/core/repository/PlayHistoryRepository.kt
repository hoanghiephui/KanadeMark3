package caios.android.kanade.core.repository

import android.net.Uri
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.database.history.PlayHistoryDao
import caios.android.kanade.core.database.history.PlayHistoryEntity
import caios.android.kanade.core.database.podcast.PodcastFeedItemEntity
import caios.android.kanade.core.database.podcast.PodcastModel
import caios.android.kanade.core.model.music.Artwork
import caios.android.kanade.core.model.music.PlayHistory
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.podcast.FeedModel
import caios.android.kanade.core.repository.podcast.FeedDiscoveryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

interface PlayHistoryRepository {

    val data: SharedFlow<List<PlayHistory>>

    fun gets(song: Song): List<PlayHistory>
    fun gets(): List<PlayHistory>

    suspend fun playHistory(song: Song): List<PlayHistory>
    suspend fun playHistories(): List<PlayHistory>

    fun add(song: Song)
}

class PlayHistoryRepositoryImpl @Inject constructor(
    private val songRepository: SongRepository,
    private val playHistoryDao: PlayHistoryDao,
    private val feedDiscoveryRepository: FeedDiscoveryRepository,
    @Dispatcher(KanadeDispatcher.IO) private val dispatcher: CoroutineDispatcher,
) : PlayHistoryRepository {

    private val cache = mutableListOf<PlayHistory>()
    private val _data = MutableStateFlow(emptyList<PlayHistory>())

    override val data: SharedFlow<List<PlayHistory>> = _data.asSharedFlow()

    override fun gets(song: Song): List<PlayHistory> =
        cache.filter { it.song == song }.sortedByDescending { it.playedAt }

    override fun gets(): List<PlayHistory> = cache.toList().sortedByDescending { it.playedAt }

    override suspend fun playHistory(song: Song): List<PlayHistory> = withContext(dispatcher) {
        val histories = playHistoryDao.loadAll().mapNotNull { it.toModel() }
        val data = histories.filter { it.song == song }

        return@withContext data.sortedBy { it.playedAt }
    }

    override suspend fun playHistories(): List<PlayHistory> = withContext(dispatcher) {
        playHistoryDao.loadAll().mapNotNull { it.toModel() }.sortedByDescending { it.playedAt }
            .also {
                cache.clear()
                cache.addAll(it)
                _data.value = cache
            }
    }

    override fun add(song: Song) {
        val data = PlayHistory(
            id = 0,
            song = song,
            playedAt = LocalDateTime.now(),
        )

        playHistoryDao.insert(data.toEntity())
    }

    private fun PlayHistory.toEntity(): PlayHistoryEntity {
        return PlayHistoryEntity(
            id = id,
            songId = song.id,
            duration = song.duration,
            createdAt = playedAt.toString(),
        )
    }

    private fun PlayHistoryEntity.toModel(): PlayHistory? {
        val song = songRepository.get(songId) ?: return null

        return PlayHistory(
            id = id,
            song = song,
            playedAt = LocalDateTime.parse(createdAt),
        )
    }


}

fun PodcastFeedItemEntity.toSong(): Song {
    return Song(
        id = songId,
        title = title,
        artistId = idPodcast,
        artist = artist,
        album = "",
        albumId = id,
        duration = duration,
        year = year,
        track = 1,
        mimeType = "audio/mpeg",
        data = data,
        dateModified = dateModified,
        uri = Uri.parse(data),
        albumArtwork = if (image != null) Artwork.Web(url = image.toString()) else Artwork.dummy(
            title
        ),
        artistArtwork = if (image != null) Artwork.Web(url = image.toString()) else Artwork.dummy(
            title
        ),
        isStream = true,
        publishDate = publishDate,
        urlImage = image
    )
}

fun PodcastModel.toFeedModel(): FeedModel =
    FeedModel(
        title = podcastFeed.title,
        imArtist = podcastFeed.author ?: "",
        imImage = podcastFeed.urlAvatar ?: ""
    )