package caios.android.kanade.feature.artist.detail

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.ScreenState
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.model.player.ShuffleMode
import caios.android.kanade.core.music.MusicController
import caios.android.kanade.core.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val musicController: MusicController,
    private val musicRepository: MusicRepository,
) : ViewModel() {
    val screenState = MutableStateFlow<ScreenState<ArtistDetailUiState>>(ScreenState.Loading)

    fun fetch(artistId: Long) {
        viewModelScope.launch {
            val artist = musicRepository.getArtist(artistId)

            screenState.value = if (artist != null) {
                ScreenState.Idle(ArtistDetailUiState(artist))
            } else {
                ScreenState.Error(message = R.string.error_no_data)
            }
        }
    }

    fun onNewPlay(songs: List<Song>, index: Int) {
        musicController.playerEvent(
            PlayerEvent.NewPlay(
                index = index,
                queue = songs,
                playWhenReady = true,
            ),
        )
    }

    fun onShufflePlay(songs: List<Song>) {
        viewModelScope.launch {
            musicRepository.setShuffleMode(ShuffleMode.ON)
            musicController.playerEvent(
                PlayerEvent.NewPlay(
                    index = 0,
                    queue = songs,
                    playWhenReady = true,
                ),
            )
        }
    }
}

@Stable
data class ArtistDetailUiState(
    val artist: Artist,
)
