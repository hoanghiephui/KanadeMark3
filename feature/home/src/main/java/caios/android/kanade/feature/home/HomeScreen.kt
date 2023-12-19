package caios.android.kanade.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.theme.center
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Queue
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.feature.home.items.HomeHeaderSection
import caios.android.kanade.feature.home.items.HomeQueueSection
import caios.android.kanade.feature.home.items.HomeRecentlyAddedAlbumsSection
import caios.android.kanade.feature.home.items.homeMostPlayedSongsSection
import caios.android.kanade.feature.home.items.homeRecentlyPlayedSongsSection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
internal fun HomeRoute(
    topMargin: Dp,
    navigateToQueue: () -> Unit,
    navigateToSongDetail: (String, List<Long>) -> Unit,
    navigateToSongMenu: (Song) -> Unit,
    navigateToAlbumDetail: (Long) -> Unit,
    navigateToAlbumMenu: (Album) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()


    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) { homeUiState ->
        if (homeUiState.recentlyAddedAlbums.isEmpty() || homeUiState.recentlyPlayedSongs.isEmpty() || homeUiState.mostPlayedSongs.isEmpty() || homeUiState.queue?.items?.isEmpty() == true) {
            HomeEmptyScreen(
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            HomeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                queue = homeUiState.queue!!,
                songs = homeUiState.songs.toImmutableList(),
                recentlyAddedAlbums = homeUiState.recentlyAddedAlbums.toImmutableList(),
                recentlyPlayedSongs = homeUiState.recentlyPlayedSongs.toImmutableList(),
                mostPlayedSongs = homeUiState.mostPlayedSongs.toImmutableList(),
                contentPadding = PaddingValues(top = topMargin),
                onClickRecentlyAdded = {
                    homeUiState.songs.sortedBy { it.addedDate }.map { it.id }.let {
                        navigateToSongDetail.invoke(
                            context.getString(R.string.song_detail_title_recently_add),
                            it
                        )
                    }
                },
                onClickHistory = {
                    scope.launch {
                        val songs = viewModel.getRecentlyPlayedSongs(9999999)
                        navigateToSongDetail.invoke(
                            context.getString(R.string.song_detail_title_history),
                            songs.map { it.id })
                    }
                },
                onClickMostPlayed = {
                    scope.launch {
                        val songs = viewModel.getMostPlayedSongs(9999999)
                        navigateToSongDetail.invoke(
                            context.getString(R.string.song_detail_title_most_played),
                            songs.map { it.first.id })
                    }
                },
                onClickPlay = viewModel::onNewPlay,
                onClickSongMenu = navigateToSongMenu,
                onClickAlbum = navigateToAlbumDetail,
                onClickAlbumMenu = navigateToAlbumMenu,
                onClickShuffle = viewModel::onShufflePlay,
                onClickQueue = navigateToQueue,
                onClickQueueItem = viewModel::onSkipToQueue,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeScreen(
    queue: Queue,
    songs: ImmutableList<Song>,
    recentlyAddedAlbums: ImmutableList<Album>,
    recentlyPlayedSongs: ImmutableList<Song>,
    mostPlayedSongs: ImmutableList<Pair<Song, Int>>,
    onClickHistory: () -> Unit,
    onClickRecentlyAdded: () -> Unit,
    onClickMostPlayed: () -> Unit,
    onClickPlay: (Int, List<Song>) -> Unit,
    onClickSongMenu: (Song) -> Unit,
    onClickAlbum: (Long) -> Unit,
    onClickAlbumMenu: (Album) -> Unit,
    onClickShuffle: (List<Song>) -> Unit,
    onClickQueue: () -> Unit,
    onClickQueueItem: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        item {
            HomeHeaderSection(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                onClickHistory = onClickHistory,
                onClickRecentlyAdded = onClickRecentlyAdded,
                onClickMostPlayed = onClickMostPlayed,
                onClickShuffle = { onClickShuffle.invoke(songs) },
            )
        }

        item {
            HomeQueueSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement(),
                queue = queue,
                onClickQueue = onClickQueue,
                onClickQueueItem = onClickQueueItem,
            )
        }

        item {
            HomeRecentlyAddedAlbumsSection(
                modifier = Modifier.fillMaxWidth(),
                albums = recentlyAddedAlbums,
                onClickMore = onClickRecentlyAdded,
                onClickAlbum = onClickAlbum,
                onClickAlbumPlay = onClickPlay,
                onClickAlbumMenu = onClickAlbumMenu,
            )
        }

        homeRecentlyPlayedSongsSection(
            songs = recentlyPlayedSongs,
            onClickSongMenu = onClickSongMenu,
            onClickPlay = onClickPlay,
            onClickMore = onClickHistory,
        )

        homeMostPlayedSongsSection(
            histories = mostPlayedSongs,
            onClickSongMenu = onClickSongMenu,
            onClickPlay = onClickPlay,
            onClickMore = onClickMostPlayed,
        )
    }
}

@Composable
private fun HomeEmptyScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            modifier = Modifier
                .padding(
                    top = 24.dp,
                )
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
            text = stringResource(R.string.home_empty_title),
            style = MaterialTheme.typography.titleLarge.center(),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            modifier = Modifier
                .padding(
                    top = 16.dp,
                    start = 24.dp,
                    end = 24.dp,
                )
                .fillMaxWidth(),
            text = stringResource(R.string.home_empty_description),
            style = MaterialTheme.typography.bodyMedium.center(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
