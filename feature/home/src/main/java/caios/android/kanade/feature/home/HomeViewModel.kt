package caios.android.kanade.feature.home

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.database.podcast.PodcastModel
import caios.android.kanade.core.design.BaseViewModel
import caios.android.kanade.core.design.NoneAction
import caios.android.kanade.core.model.ScreenState
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Playlist
import caios.android.kanade.core.model.music.Queue
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.MusicConfig
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.model.player.ShuffleMode
import caios.android.kanade.core.music.MusicController
import caios.android.kanade.core.repository.LastFmRepository
import caios.android.kanade.core.repository.MusicRepository
import caios.android.kanade.core.repository.PlaylistRepository
import caios.android.kanade.core.repository.podcast.FeedDiscoveryRepository
import caios.android.kanade.core.repository.toSong
import com.applovin.sdk.AppLovinSdk
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random
import javax.inject.Inject

@Stable
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicController: MusicController,
    private val musicRepository: MusicRepository,
    playlistRepository: PlaylistRepository,
    lastFmRepository: LastFmRepository,
    feedDiscoveryRepository: FeedDiscoveryRepository,
    @Dispatcher(KanadeDispatcher.IO)
    private val ioDispatcher: CoroutineDispatcher,
    @Dispatcher(KanadeDispatcher.Default)
    defaultDispatcher: CoroutineDispatcher,
    appLoVinSdk: AppLovinSdk
) : BaseViewModel<NoneAction>(defaultDispatcher, appLoVinSdk) {


    val screenState = combine(
        musicRepository.config,
        musicController.currentQueue,
        playlistRepository.data,
        lastFmRepository.albumDetails,
        musicRepository.updateFlag,
        feedDiscoveryRepository.loadLatestAdd().flowOn(ioDispatcher),
        feedDiscoveryRepository.loadAddItem().flowOn(ioDispatcher).map { podcastFeedItemEntities ->
            podcastFeedItemEntities.map { it.toSong() }
        }
    ) { data ->
        val config = data[0] as MusicConfig
        val queue = data[1] as Queue
        val playlist = data[2] as List<*>
        val subscribedFeeds = data[5] as List<*>
        val lastFeedItem = data[6] as List<*>
        musicRepository.fetchSongsPodcast(lastFeedItem.filterIsInstance<Song>())
        withContext(ioDispatcher) {
            musicRepository.fetchSongs(config)
            musicRepository.fetchAlbumArtwork()
        }

        val songs = musicRepository.sortedSongs(config)
        val albums = musicRepository.sortedAlbums(config)
        val recentlyAddedAlbums = albums.sortedBy { it.addedDate }.take(10)
        val favorite = playlist.filterIsInstance<Playlist>().find { it.isSystemPlaylist }

        ScreenState.Idle(
            HomeUiState(
                queue = queue,
                songs = songs,
                recentlyAddedAlbums = recentlyAddedAlbums,
                recentlyPlayedSongs = getRecentlyPlayedSongs(6),
                mostPlayedSongs = getMostPlayedSongs(6),
                favoriteSongs = favorite?.songs ?: emptyList(),
                subscribedFeeds = subscribedFeeds.filterIsInstance<PodcastModel>()
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    suspend fun getRecentlyPlayedSongs(take: Int): List<Song> {
        musicRepository.fetchPlayHistory()
        return musicRepository.playHistory
            .map { it.song }
            .distinct()
            .take(take)
    }

    suspend fun getMostPlayedSongs(take: Int): List<Pair<Song, Int>> {
        musicRepository.fetchPlayHistory()
        return musicRepository.getPlayedCount()
            .map { it.toPair() }
            .sortedByDescending { it.second }
            .take(take)
    }

    fun onNewPlay(index: Int, queue: List<Song>) {
        musicController.playerEvent(
            PlayerEvent.NewPlay(
                index = index,
                queue = queue,
                playWhenReady = true,
            ),
        )
    }

    fun onShufflePlay(queue: List<Song>) {
        viewModelScope.launch {
            musicRepository.setShuffleMode(ShuffleMode.ON)
            musicController.playerEvent(
                PlayerEvent.NewPlay(
                    index = Random().nextInt(queue.size),
                    queue = queue,
                    playWhenReady = true,
                ),
            )
        }
    }

    fun onSkipToQueue(index: Int) {
        musicController.playerEvent(
            PlayerEvent.SkipToQueue(index),
        )
    }


}

@Stable
data class HomeUiState(
    val queue: Queue?,
    val songs: List<Song>,
    val recentlyAddedAlbums: List<Album>,
    val recentlyPlayedSongs: List<Song>,
    val mostPlayedSongs: List<Pair<Song, Int>>,
    val favoriteSongs: List<Song>,
    val subscribedFeeds: List<PodcastModel>
)
