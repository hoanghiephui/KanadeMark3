package caios.android.kanade.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Playlist
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.feature.search.top.SearchRoute
import caios.android.kanade.feature.search.top.SearchViewModel
import kotlinx.coroutines.launch

@Suppress("ViewModelInjection")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationGraphicsApi::class)
@Composable
fun PodcastTopBar(
    active: Boolean,
    yOffset: Dp,
    isEnableBackHandler: Boolean,
    isEnableYTMusic: Boolean,
    onChangeActive: (Boolean) -> Unit,
    onClickDrawerMenu: () -> Unit,
    navigateToArtistDetail: (Long) -> Unit,
    navigateToAlbumDetail: (Long) -> Unit,
    navigateToPlaylistDetail: (Long) -> Unit,
    navigateToSongMenu: (Song) -> Unit,
    navigateToArtistMenu: (Artist) -> Unit,
    navigateToAlbumMenu: (Album) -> Unit,
    navigateToPlaylistMenu: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
    isPodcast: Boolean,
    idSearchBy: Int
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val searchViewModel = hiltViewModel<SearchViewModel>()

    val image = AnimatedImageVector.animatedVectorResource(R.drawable.av_drawer_to_arrow)
    var atEnd by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val navigationBarHeight = with(density) { TopAppBarDefaults.windowInsets.getTop(density).toFloat().toDp() }
    val toolbarPadding by animateDpAsState(
        targetValue = if (active) 0.dp else navigationBarHeight,
        label = "toolbarPadding",
        animationSpec = tween(400),
    )

    LaunchedEffect(active) {
        atEnd = active
        query = ""
    }

    LaunchedEffect(query) {
        if (!isEnableYTMusic) {
            searchViewModel.search(listOf(query), isPodcast, idSearchBy)
        }
    }

    Column(
        modifier = modifier
            .offset(y = yOffset)
            .padding(top = toolbarPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(navigationBarHeight - toolbarPadding)
                .alpha(1f - toolbarPadding / navigationBarHeight)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        )

        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {
                if (isEnableYTMusic) {
                    scope.launch {
                        searchViewModel.search(listOf(query), isPodcast, idSearchBy)
                    }
                }
            },
            active = active,
            onActiveChange = onChangeActive,
            colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            windowInsets = WindowInsets(0, 0, 0, 0),
            placeholder = { Text(stringResource(if (isPodcast)
                R.string.search_podcast_title else R.string.search_title)) },
            trailingIcon = {
                 if (query.isNotBlank()) {
                     IconButton(onClick = { query = "" }) {
                         Icon(
                             modifier = Modifier
                                 .padding(6.dp),
                             imageVector = Icons.Default.Clear,
                             contentDescription = null,
                             tint = MaterialTheme.colorScheme.onSurfaceVariant,
                         )
                     }

                 }
            },
            leadingIcon = {
                IconButton(onClick = {
                    if (active) {
                        query = ""
                        onChangeActive.invoke(false)
                    } else {
                        onClickDrawerMenu.invoke()
                    }
                }) {
                    Icon(
                        modifier = Modifier
                            .padding(6.dp),
                        painter = rememberAnimatedVectorPainter(image, atEnd),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        ) {
            BackHandler(isEnableBackHandler) {
                onChangeActive.invoke(false)
            }

            SearchRoute(
                modifier = Modifier.fillMaxWidth(),
                viewModel = searchViewModel,
                navigateToArtistDetail = navigateToArtistDetail,
                navigateToAlbumDetail = navigateToAlbumDetail,
                navigateToPlaylistDetail = navigateToPlaylistDetail,
                navigateToSongMenu = navigateToSongMenu,
                navigateToArtistMenu = navigateToArtistMenu,
                navigateToAlbumMenu = navigateToAlbumMenu,
                navigateToPlaylistMenu = navigateToPlaylistMenu,
                isSearchPodcast = isPodcast
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    PodcastTopBar(
        active = false,
        yOffset = 0.dp,
        isEnableBackHandler = false,
        isEnableYTMusic = false,
        onChangeActive = { },
        onClickDrawerMenu = { },
        navigateToArtistDetail = { },
        navigateToAlbumDetail = { },
        navigateToPlaylistDetail = { },
        navigateToSongMenu = { },
        navigateToArtistMenu = { },
        navigateToAlbumMenu = { },
        navigateToPlaylistMenu = { },
        isPodcast = true,
        idSearchBy = 0
    )
}
