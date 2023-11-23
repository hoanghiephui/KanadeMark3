package caios.android.kanade.feature.home

import androidx.annotation.AnyThread
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import caios.android.kanade.core.common.network.BaseViewModel
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.common.network.NoneAction
import caios.android.kanade.core.common.network.Result
import caios.android.kanade.core.common.network.asFlowResult
import caios.android.kanade.core.common.network.data
import caios.android.kanade.core.common.network.extension.safeCollect
import caios.android.kanade.core.common.network.isError
import caios.android.kanade.core.common.network.isLoading
import caios.android.kanade.core.common.network.onResultError
import caios.android.kanade.core.model.ScreenState
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Playlist
import caios.android.kanade.core.model.music.Queue
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.MusicConfig
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.model.player.ShuffleMode
import caios.android.kanade.core.model.podcast.EntryItem
import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import caios.android.kanade.core.music.MusicController
import caios.android.kanade.core.repository.LastFmRepository
import caios.android.kanade.core.repository.MusicRepository
import caios.android.kanade.core.repository.PlaylistRepository
import caios.android.kanade.core.ui.error.ErrorsDispatcher
import caios.android.kanade.core.ui.error.ImmutableList
import com.podcast.core.usecase.ItunesFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random
import javax.inject.Inject

@Stable
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicController: MusicController,
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val lastFmRepository: LastFmRepository,
    private val feedDiscoveryUseCase: ItunesFeedUseCase,
    @Dispatcher(KanadeDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
    @Dispatcher(KanadeDispatcher.Default)
    defaultDispatcher: CoroutineDispatcher,
    private val errorsDispatcher: ErrorsDispatcher,
    private val movieItemMapper: DiscoverFeedItemMapper,
) : BaseViewModel<NoneAction>(defaultDispatcher) {

    private val feedState = MutableStateFlow(emptyList<EntryItem>())
    private val fetchNewFeedResultState = MutableStateFlow<Result<ItunesTopPodcastResponse>?>(null)
    val uiState: StateFlow<DiscoverUiState> =
        combine(
            feedState.map(movieItemMapper::map),
            fetchNewFeedResultState
        ) { items, podcastResponseResult ->
            createDiscoverUiState(
                items = items,
                podcastResponseResult = podcastResponseResult
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = DiscoverUiState.None,
        )

    @AnyThread
    private fun createDiscoverUiState(
        items: ImmutableList<EntryItem>,
        podcastResponseResult: Result<ItunesTopPodcastResponse>?
    ): DiscoverUiState = when {
        items.isNotEmpty() -> DiscoverUiState.Discover(
            items = items
        )

        podcastResponseResult.isLoading -> DiscoverUiState.Loading
        podcastResponseResult.isError -> DiscoverUiState.Retry
        else -> DiscoverUiState.None
    }

    val screenState = combine(
        musicRepository.config,
        musicController.currentQueue,
        playlistRepository.data,
        lastFmRepository.albumDetails,
        musicRepository.updateFlag,
    ) { data ->
        val config = data[0] as MusicConfig
        val queue = data[1] as Queue
        val playlist = data[2] as List<*>

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

    init {
        fetchNewDiscoverPodcast()
    }

    @AnyThread
    private fun fetchNewDiscoverPodcast() {
        viewModelScope.launch(defaultDispatcher) {
            val currentState = fetchNewFeedResultState.getAndUpdate { Result.Loading() }
            if (currentState !is Result.Loading) {
                asFlowResult { feedDiscoveryUseCase.getTopPodcast("US", 25) }
                    .onResultError(errorsDispatcher::dispatch)
                    .safeCollect(
                        onEach = { result ->
                            feedState.update { it + result.data?.feed?.entry.orEmpty() }
                            fetchNewFeedResultState.emit(result)
                        },
                        onError = errorsDispatcher::dispatch,
                    )
            }
        }
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
)
