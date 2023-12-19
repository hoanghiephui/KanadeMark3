package com.podcast.discover.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.core.ui.music.AlbumDetailHeader
import caios.android.kanade.core.ui.music.AlbumHolder
import caios.android.kanade.core.ui.music.EpisodeDetailHeader
import caios.android.kanade.core.ui.music.SongDetailHeader
import caios.android.kanade.core.ui.music.SongHolder
import caios.android.kanade.core.ui.view.CoordinatorData
import caios.android.kanade.core.ui.view.CoordinatorScaffold

@Composable
fun OnlineFeedRoute(
    modifier: Modifier = Modifier,
    viewModel: OnlineFeedViewModel = hiltViewModel(),
    terminate: () -> Unit,
    feedId: String,
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = feedId, block = {
        viewModel.getLookFeed(feedId)
    })

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = { },
    ) {
        OnlineFeedScreen(
            modifier = Modifier.fillMaxSize(),
            artist = it,
            onClickMenu = {},
            onTerminate = terminate
        )
    }
}

@Composable
private fun OnlineFeedScreen(
    modifier: Modifier = Modifier,
    artist: Artist,
    onTerminate: () -> Unit,
    onClickMenu: (Artist) -> Unit
) {
    val context = LocalContext.current
    var isVisibleFAB by remember { mutableStateOf(false) }
    val coordinatorData = remember {
        CoordinatorData.Artist(
            title = artist.artist,
            summary = context.getString(R.string.unit_song, artist.songs.size),
            artwork = artist.artwork,
        )
    }

    Box(modifier) {
        CoordinatorScaffold(
            modifier = Modifier.fillMaxSize(),
            data = coordinatorData,
            onClickNavigateUp = onTerminate,
            onClickMenu = { onClickMenu.invoke(artist) },
        ) {
            item {
                EpisodeDetailHeader(
                    modifier = Modifier.fillMaxWidth(),
                    onClickSeeAll = { },
                    size = artist.songs.size
                )
            }

            itemsIndexed(
                items = artist.songs,
                key = { _, song -> song.id },
            ) { index, song ->
                SongHolder(
                    modifier = Modifier.fillMaxWidth(),
                    song = song,
                    onClickHolder = { },
                    onClickMenu = { },
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
    }
}
