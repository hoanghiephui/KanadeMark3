package caios.android.kanade.core.music

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import caios.android.kanade.core.model.ThemeConfig
import caios.android.kanade.core.model.UserData
import caios.android.kanade.core.model.music.Lyrics
import caios.android.kanade.core.model.music.Playlist
import caios.android.kanade.core.model.music.Queue
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.MusicConfig
import caios.android.kanade.core.model.player.MusicOrder
import caios.android.kanade.core.model.player.MusicOrderOption
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.model.player.PlayerState
import caios.android.kanade.core.model.player.RepeatMode
import caios.android.kanade.core.model.player.ShuffleMode
import caios.android.kanade.core.music.upgrade.BackwardCompatibleChecker
import caios.android.kanade.core.repository.LyricsRepository
import caios.android.kanade.core.repository.MusicRepository
import caios.android.kanade.core.repository.UserDataRepository
import caios.android.kanade.core.repository.di.LyricsMusixmatch
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@Stable
@HiltViewModel
class MusicViewModel @Inject constructor(
    private val backwardCompatibleChecker: BackwardCompatibleChecker,
    private val musicController: MusicController,
    private val musicRepository: MusicRepository,
    private val userDataRepository: UserDataRepository,
    @LyricsMusixmatch private val lyricsRepository: LyricsRepository,
) : ViewModel() {

    var uiState by mutableStateOf(MusicUiState())
        private set

    init {
        fetch()

        viewModelScope.launch {
            combine(
                userDataRepository.userData,
                musicRepository.config,
                musicController.currentSong,
                musicController.currentQueue,
                musicController.playerState,
                musicController.playerPosition,
                musicController.isAnalyzing,
                lyricsRepository.data,
            ) { data ->
                val userData = data[0] as UserData
                val config = data[1] as MusicConfig
                val song = data[2] as Song?
                val queue = data[3] as Queue?
                val state = data[4] as PlayerState
                val position = data[5] as Long
                val isAnalyzing = data[6] as Boolean

                uiState.copy(
                    userData = userData,
                    song = song,
                    lyrics = song?.let { musicRepository.getLyrics(it) },
                    queueItems = if (queue?.items?.isNotEmpty() == true) queue.items else if (song != null) listOf(song) else emptyList(),
                    queueIndex = queue?.index ?: if (song != null) 1 else 0,
                    progress = position,
                    state = state,
                    shuffleMode = config.shuffleMode,
                    repeatMode = config.repeatMode,
                    songOrder = config.songOrder,
                    artistOrder = config.artistOrder,
                    albumOrder = config.albumOrder,
                    playlistOrder = config.playlistOrder,
                    isAnalyzing = isAnalyzing,
                )
            }.collect {
                uiState = it
            }
        }
    }

    fun initKanadeId() {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()

            userDataRepository.setKanadeId(id)
            Timber.d("Initailize KanadeID to $id")
        }
    }

    fun initYoutubeDL(context: Context) {
        viewModelScope.launch {
            runCatching {
                YoutubeDL.init(context)
                FFmpeg.init(context)
                Aria2c.init(context)

                uiState = uiState.copy(isEnableYoutubeDL = true)
            }
        }
    }

    fun fetch() {
        viewModelScope.launch {
            musicRepository.fetchSongs()
            musicRepository.fetchArtists()
            musicRepository.fetchAlbums()
            musicRepository.fetchPlaylist()
            musicRepository.fetchAlbumArtwork()
            musicRepository.fetchArtistArtwork()
            backwardCompatibleChecker.check()

            uiState = uiState.copy(isReadyToFmService = musicRepository.songs.isNotEmpty())

            Timber.d("Fetch library. Songs: ${musicRepository.songs.size}, Artists: ${musicRepository.artists.size}, Albums: ${musicRepository.albums.size}")
        }
    }

    fun setControllerState(isExpanded: Boolean) {
        uiState = uiState.copy(isExpandedController = isExpanded)
    }

    fun setPlusDialogDisplayed(isDisplayed: Boolean) {
        uiState = uiState.copy(isDisplayedPlusDialog = isDisplayed)
    }

    fun playerEvent(event: PlayerEvent) {
        musicController.playerEvent(event)
    }

    suspend fun fetchFavorite(song: Song): Boolean {
        return musicRepository.isFavorite(song)
    }

    suspend fun onFavorite(song: Song) {
        if (fetchFavorite(song)) {
            musicRepository.removeFromFavorite(song)
        } else {
            musicRepository.addToFavorite(song)
        }
    }

    fun addToQueue(songs: List<Song>, index: Int? = null) {
        musicController.addToQueue(songs, index)
    }

    fun removePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            musicRepository.removePlaylist(playlist)
        }
    }

    fun setSortOrder(order: MusicOrder) {
        viewModelScope.launch {
            when (order.option) {
                is MusicOrderOption.Song -> musicRepository.setSongOrder(order)
                is MusicOrderOption.Artist -> musicRepository.setArtistOrder(order)
                is MusicOrderOption.Album -> musicRepository.setAlbumOrder(order)
                is MusicOrderOption.Playlist -> musicRepository.setPlaylistOrder(order)
            }
        }
    }
}

@Stable
data class MusicUiState(
    val userData: UserData? = null,
    val song: Song? = null,
    val lyrics: Lyrics? = null,
    val queueItems: List<Song> = emptyList(),
    val queueIndex: Int = 0,
    val progress: Long = 0L,
    val state: PlayerState = PlayerState.Initialize,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val songOrder: MusicOrder = MusicOrder.songDefault(),
    val artistOrder: MusicOrder = MusicOrder.artistDefault(),
    val albumOrder: MusicOrder = MusicOrder.albumDefault(),
    val playlistOrder: MusicOrder = MusicOrder.playlistDefault(),
    val isExpandedController: Boolean = false,
    val isDisplayedPlusDialog: Boolean = false,
    val isReadyToFmService: Boolean = false,
    val isAnalyzing: Boolean = false,
    val isEnableYoutubeDL: Boolean = false,
) {
    val isPlaying
        get() = (state == PlayerState.Playing)

    val isLoading
        get() = (state == PlayerState.Buffering || state == PlayerState.Initialize)

    val progressParent: Float
        get() {
            val duration = song?.duration ?: return 0f
            val parent = progress / duration.toDouble()

            return parent.coerceIn(0.0, 1.0).toFloat()
        }

    val progressString: String
        get() {
            val progress = progress / 1000
            val minute = progress / 60
            val second = progress % 60

            return "%02d:%02d".format(minute, second)
        }

    companion object {
        @Composable
        fun UserData.isDarkMode(): Boolean {
            return when (themeConfig) {
                ThemeConfig.Light -> false
                ThemeConfig.Dark -> true
                else -> isSystemInDarkTheme()
            }
        }
    }
}
