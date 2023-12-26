package com.podcast.discover.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.core.ui.music.EpisodeDetailHeader
import caios.android.kanade.core.ui.music.PodcastItemHolder
import caios.android.kanade.core.ui.view.CoordinatorData
import caios.android.kanade.core.ui.view.PodcastCoordinatorScaffold

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
            onTerminate = terminate,
            onClickSongHolder = viewModel::onNewPlay,
            onClickAddToQueue = viewModel::onAddToQuote,
            onClickDownload = viewModel::onDownloadSong,
            onClickPause = {
                viewModel.playerEvent(PlayerEvent.Pause)
            }
        )
    }
}

@Composable
private fun OnlineFeedScreen(
    modifier: Modifier = Modifier,
    artist: Artist,
    onTerminate: () -> Unit,
    onClickMenu: (Artist) -> Unit,
    onClickSongHolder: (List<Song>, Int) -> Unit,
    onClickAddToQueue: (Song) -> Unit,
    onClickDownload: (Song) -> Unit,
    onClickPause: () -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    val coordinatorData = remember {
        CoordinatorData.Podcast(
            title = artist.artist,
            summary = artist.description ?: "",
            artwork = artist.artwork,
            author = artist.author ?: ""
        )
    }

    PodcastCoordinatorScaffold(
        modifier = modifier.fillMaxSize(),
        data = coordinatorData,
        onClickNavigateUp = onTerminate,
        onClickMenu = { onClickMenu.invoke(artist) },
    ) {
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateContentSize(),
                text = artist.description ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (!expanded) 3 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(text = if (expanded) "Show less" else "Show more")
                }
            }
        }
        item {
            EpisodeDetailHeader(
                modifier = Modifier.fillMaxWidth(),
                onClickSeeAll = { },
                size = artist.songs.size
            )
        }

        itemsIndexed(
            items = artist.songs.take(6),
            key = { _, song -> song.id },
        ) { index, song ->
            PodcastItemHolder(
                modifier = Modifier.fillMaxWidth(),
                song = song,
                onClickPlay = { onClickSongHolder.invoke(artist.songs, index) },
                onClickMenu = { },
                onClickDownload = onClickDownload,
                onClickAddToQueue = onClickAddToQueue,
                onClickPause = onClickPause
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
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
