package caios.android.kanade.feature.search.top.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.theme.bold
import caios.android.kanade.core.design.theme.center
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Playlist
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.podcast.PodcastSearchResult
import caios.android.kanade.feature.search.top.SearchUiState

@Composable
internal fun SearchResultSection(
    uiState: SearchUiState,
    onClickSong: (List<Song>, Int) -> Unit,
    onClickArtist: (Artist) -> Unit,
    onClickAlbum: (Album) -> Unit,
    onClickPlaylist: (Playlist) -> Unit,
    onClickSongMenu: (Song) -> Unit,
    onClickArtistMenu: (Artist) -> Unit,
    onClickAlbumMenu: (Album) -> Unit,
    onClickPlaylistMenu: (Playlist) -> Unit,
    navigateToPodcast: (PodcastSearchResult) -> Unit,
    modifier: Modifier = Modifier,
    isSearchPodcast: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                keyboardController?.hide()
                return Offset.Zero
            }
        }
    }
    LazyColumn(
        modifier = modifier.nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 8.dp),
    ) {
        if (uiState.resultYTMusic.isNotEmpty()) {
            itemsIndexed(
                items = uiState.resultYTMusic,
                key = { _, ytmusicSearch -> ytmusicSearch.hashCode() },
            ) { index, ytmusicSearch ->
                Text(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    text = ytmusicSearch.title ?: "unknown",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        if (uiState.resultSongs.isNotEmpty()) {
            item {
                SearchHeaderItem(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    type = HeaderItemType.Song,
                    size = uiState.resultSongs.size,
                )
            }

            itemsIndexed(
                items = uiState.resultSongs,
                key = { _, song -> song.id },
            ) { index, song ->
                SearchSongHolder(
                    modifier = Modifier.fillMaxWidth(),
                    song = song,
                    range = uiState.resultSongsRangeMap[song.id] ?: 0..0,
                    onClickHolder = { onClickSong.invoke(uiState.resultSongs, index) },
                    onClickMenu = onClickSongMenu,
                )
            }
        }

        if (uiState.resultArtists.isNotEmpty()) {
            item {
                SearchHeaderItem(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    type = HeaderItemType.Artist,
                    size = uiState.resultArtists.size,
                )
            }

            items(
                items = uiState.resultArtists,
                key = { artist -> artist.artistId },
            ) {
                SearchArtistHolder(
                    modifier = Modifier.fillMaxWidth(),
                    artist = it,
                    range = uiState.resultArtistsRangeMap[it.artistId] ?: 0..0,
                    onClickHolder = { onClickArtist.invoke(it) },
                    onClickMenu = onClickArtistMenu,
                )
            }
        }

        if (uiState.resultAlbums.isNotEmpty()) {
            item {
                SearchHeaderItem(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    type = HeaderItemType.Album,
                    size = uiState.resultAlbums.size,
                )
            }

            items(
                items = uiState.resultAlbums,
                key = { album -> album.albumId },
            ) {
                SearchAlbumHolder(
                    modifier = Modifier.fillMaxWidth(),
                    album = it,
                    range = uiState.resultAlbumsRangeMap[it.albumId] ?: 0..0,
                    onClickHolder = { onClickAlbum.invoke(it) },
                    onClickMenu = onClickAlbumMenu,
                )
            }
        }

        if (uiState.resultPlaylists.isNotEmpty()) {
            item {
                SearchHeaderItem(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    type = HeaderItemType.Playlist,
                    size = uiState.resultPlaylists.size,
                )
            }

            items(
                items = uiState.resultPlaylists,
                key = { playlist -> playlist.id },
            ) {
                SearchPlaylistHolder(
                    modifier = Modifier.fillMaxWidth(),
                    playlist = it,
                    range = uiState.resultPlaylistsRangeMap[it.id] ?: 0..0,
                    onClickHolder = { onClickPlaylist.invoke(it) },
                    onClickMenu = onClickPlaylistMenu,
                )
            }
        }

        if (uiState.resultSearchPodcast.isNotEmpty()) {
            items(
                items = uiState.resultSearchPodcast,
                key = { podcast -> podcast.id },
            ) {
                SearchPodcastHolder(
                    modifier = Modifier.fillMaxWidth(),
                    podcastSearchResult = it,
                    range = 0..0,
                    onClickHolder = {
                        keyboardController?.hide()
                        navigateToPodcast.invoke(it)
                    }
                )
            }
        }
    }
}
