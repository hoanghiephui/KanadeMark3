package caios.android.kanade.feature.search.top

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.theme.center
import caios.android.kanade.core.model.ScreenState
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Playlist
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.feature.search.top.items.SearchResultSection
import timber.log.Timber

@Composable
fun SearchRoute(
    navigateToArtistDetail: (Long) -> Unit,
    navigateToAlbumDetail: (Long) -> Unit,
    navigateToPlaylistDetail: (Long) -> Unit,
    navigateToSongMenu: (Song) -> Unit,
    navigateToArtistMenu: (Artist) -> Unit,
    navigateToAlbumMenu: (Album) -> Unit,
    navigateToPlaylistMenu: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    isSearchPodcast: Boolean
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    SearchScreen(
        modifier = modifier,
        screenState = screenState,
        onClickSong = viewModel::onNewPlay,
        onClickArtist = { navigateToArtistDetail.invoke(it.artistId) },
        onClickAlbum = { navigateToAlbumDetail.invoke(it.albumId) },
        onClickPlaylist = { navigateToPlaylistDetail.invoke(it.id) },
        onClickSongMenu = navigateToSongMenu,
        onClickArtistMenu = navigateToArtistMenu,
        onClickAlbumMenu = navigateToAlbumMenu,
        onClickPlaylistMenu = navigateToPlaylistMenu,
        isSearchPodcast = isSearchPodcast
    )
}

@Composable
private fun SearchScreen(
    screenState: ScreenState<SearchUiState>,
    onClickSong: (List<Song>, Int) -> Unit,
    onClickArtist: (Artist) -> Unit,
    onClickAlbum: (Album) -> Unit,
    onClickPlaylist: (Playlist) -> Unit,
    onClickSongMenu: (Song) -> Unit,
    onClickArtistMenu: (Artist) -> Unit,
    onClickAlbumMenu: (Album) -> Unit,
    onClickPlaylistMenu: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
    isSearchPodcast: Boolean
) {
    AsyncLoadContents(
        modifier = modifier.fillMaxSize(),
        screenState = screenState,
    ) { uiState ->
        if (
            (uiState.resultSongs.isEmpty() &&
                    uiState.resultAlbums.isEmpty() &&
                    uiState.resultArtists.isEmpty() &&
                    uiState.resultPlaylists.isEmpty() &&
                    uiState.resultSearchPodcast.isEmpty()) ||
            uiState.keywords.all { it.isBlank() }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth(),
                    painter = painterResource(R.drawable.vec_empty_music),
                    contentDescription = "empty music",
                )

                Text(
                    modifier = Modifier
                        .padding(
                            top = 32.dp,
                            start = 24.dp,
                            end = 24.dp,
                        )
                        .fillMaxWidth(),
                    text = stringResource(R.string.search_not_result),
                    style = MaterialTheme.typography.titleMedium.center(),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    modifier = Modifier
                        .padding(
                            top = 8.dp,
                            start = 24.dp,
                            end = 24.dp,
                        )
                        .fillMaxWidth(),
                    text = stringResource(R.string.type_to_search),
                    style = MaterialTheme.typography.bodyMedium.center(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            SearchResultSection(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onClickSong = onClickSong,
                onClickArtist = onClickArtist,
                onClickAlbum = onClickAlbum,
                onClickPlaylist = onClickPlaylist,
                onClickSongMenu = onClickSongMenu,
                onClickArtistMenu = onClickArtistMenu,
                onClickAlbumMenu = onClickAlbumMenu,
                onClickPlaylistMenu = onClickPlaylistMenu,
                isSearchPodcast = isSearchPodcast
            )
        }

    }
}
