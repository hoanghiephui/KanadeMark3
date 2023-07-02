package caios.android.kanade.feature.playlist.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.music.Playlist
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.core.ui.music.IndexedSongHolder
import caios.android.kanade.core.ui.view.CoordinatorData
import caios.android.kanade.core.ui.view.CoordinatorScaffold
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun PlaylistDetailRoute(
    playlistId: Long,
    navigateToPlaylistMenu: (Playlist) -> Unit,
    navigateToSongMenu: (Song) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    LaunchedEffect(playlistId) {
        viewModel.fetch(playlistId)
    }

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) { uiState ->
        if (uiState != null) {
            PlaylistDetailScreen(
                playlist = uiState.playlist,
                onClickPlay = viewModel::onNewPlay,
                onClickMenu = navigateToPlaylistMenu,
                onClickSongMenu = navigateToSongMenu,
                onTerminate = terminate,
            )
        }
    }
}

@Composable
private fun PlaylistDetailScreen(
    playlist: Playlist,
    onClickPlay: (List<Song>, Int) -> Unit,
    onClickMenu: (Playlist) -> Unit,
    onClickSongMenu: (Song) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberReorderableLazyListState(
        onMove = { from, to -> }
    )

    var isVisibleFAB by remember { mutableStateOf(false) }
    val coordinatorData = CoordinatorData.Playlist(
        title = playlist.name,
        summary = stringResource(R.string.unit_song, playlist.songs.size),
        artworks = playlist.songs.map { it.artwork },
    )

    LaunchedEffect(playlist) {
        isVisibleFAB = true
    }

    Box(modifier) {
        CoordinatorScaffold(
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state)
                .detectReorderAfterLongPress(state),
            listState = state.listState,
            data = coordinatorData,
            onClickNavigateUp = onTerminate,
            onClickMenu = { onClickMenu.invoke(playlist) },
        ) {
            items(
                items = playlist.items.toList(),
                key = { item -> item.id },
            ) { item ->
                ReorderableItem(
                    reorderableState = state,
                    key = { item.id },
                ) {
                    IndexedSongHolder(
                        modifier = Modifier.fillMaxWidth(),
                        song = item.song,
                        index = item.index,
                        state = state,
                        onClickHolder = { onClickPlay.invoke(playlist.songs, it) },
                        onClickMenu = onClickSongMenu,
                    )
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp),
                )
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            visible = isVisibleFAB,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { onClickPlay.invoke(playlist.songs, 0) },
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                )
            }
        }
    }
}
