package caios.android.kanade.feature.playlist.top

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.music.Playlist
import caios.android.kanade.core.model.player.MusicOrder
import caios.android.kanade.core.model.player.MusicOrderOption
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.core.ui.music.PlaylistHolder
import caios.android.kanade.core.ui.music.SortInfo
import caios.android.kanade.core.ui.view.FixedWithEdgeSpace
import caios.android.kanade.core.ui.view.KanadeTopAppBar
import caios.android.kanade.core.ui.view.itemsWithEdgeSpace
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaylistTopRoute(
    topMargin: Dp,
    navigateToPlaylistDetail: (Long) -> Unit,
    navigateToPlaylistMenu: (Playlist) -> Unit,
    navigateToPlaylistEdit: () -> Unit,
    navigateToSort: (KClass<*>) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistTopViewModel = hiltViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val state = rememberTopAppBarState()
    val behavior = TopAppBarDefaults.pinnedScrollBehavior(state)
    Scaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            KanadeTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.navigation_playlist),
                behavior = behavior,
                onTerminate = onTerminate,
            )
        },
    ) { paddingValues ->
        AsyncLoadContents(
            modifier = modifier,
            screenState = screenState,
        ) { uiState ->
            PlaylistTopScreen(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                contentPadding = paddingValues,
                playlists = uiState.playlists.toImmutableList(),
                sortOrder = uiState.sortOrder,
                onClickSort = { navigateToSort.invoke(MusicOrderOption.Playlist::class) },
                onClickEdit = navigateToPlaylistEdit,
                onClickPlaylist = navigateToPlaylistDetail,
                onClickPlay = viewModel::onNewPlay,
                onClickMenu = navigateToPlaylistMenu,
            )
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PlaylistTopScreen(
    playlists: ImmutableList<Playlist>,
    sortOrder: MusicOrder,
    onClickSort: (MusicOrder) -> Unit,
    onClickEdit: () -> Unit,
    onClickPlaylist: (Long) -> Unit,
    onClickPlay: (Playlist) -> Unit,
    onClickMenu: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var isVisibleFAB by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        isVisibleFAB = true
    }

    Box(modifier) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding()),
            contentPadding = PaddingValues(0.dp),
            columns = FixedWithEdgeSpace(
                count = 2,
                edgeSpace = 8.dp,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SortInfo(
                    sortOrder = sortOrder,
                    itemSize = playlists.size,
                    onClickSort = onClickSort,
                )
            }

            itemsWithEdgeSpace(
                spanCount = 2,
                items = playlists,
                key = { it.id },
            ) { playlist ->
                PlaylistHolder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement(),
                    playlist = playlist,
                    onClickHolder = { onClickPlaylist.invoke(playlist.id) },
                    onClickPlay = { onClickPlay.invoke(playlist) },
                    onClickMenu = { onClickMenu.invoke(playlist) },
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
                onClick = { onClickEdit.invoke() },
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                )
            }
        }
    }
}
