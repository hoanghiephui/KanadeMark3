package caios.android.kanade.feature.search.top

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caios.android.kanade.core.model.ScreenState
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Playlist
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.feature.search.top.items.SearchResultSection

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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncLoadContents(
            modifier = Modifier.fillMaxSize(),
            screenState = screenState,
        ) { uiState ->
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
            )
        }
    }
}
