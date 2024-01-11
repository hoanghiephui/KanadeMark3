package com.podcast.discover.detail

import android.app.Activity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import caios.android.kanade.core.common.network.extension.shouldAllowPermission
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.ui.TrackScreenViewEvent
import caios.android.kanade.core.ui.music.PodcastItemHolder
import caios.android.kanade.core.ui.view.KanadeTopAppBar
import com.podcast.core.network.util.PodcastDownloader
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun FeedMoreRouter(
    feed: ImmutableList<Song>,
    onTerminate: () -> Unit,
    showSnackBar: (String) -> Unit,
    viewModel: OnlineFeedViewModel = hiltViewModel(),
    title: String?,
) {
    val context = LocalContext.current
    val permission = (context as Activity).shouldAllowPermission()
    val mDownloadStatus: SnapshotStateMap<Long, Int> = remember {
        mutableStateMapOf()
    }
    val mDownloadProgress: SnapshotStateMap<Long, Float> = remember {
        mutableStateMapOf()
    }
    FeedMoreScreen(
        title = title,
        feed = feed,
        onTerminate = onTerminate,
        onClickSongHolder = viewModel::onNewPlay,
        onClickAddToQueue = viewModel::onAddToQuote,
        onClickDownload = {
            val message = viewModel.downloadPodcast(
                song = it,
                permission = !permission,
                context = context,
            ) { downloadProgress, downloadStatus ->
                mDownloadStatus[it.id] = downloadStatus
                mDownloadProgress[it.id] = downloadProgress
            }
            showSnackBar.invoke(message)
        },
        onClickPause = {
            viewModel.playerEvent(PlayerEvent.Pause)
        },
        onClickCancelDownload = {
            val id = viewModel.download.getRunningDownload(it.id)?.downloadId
            viewModel.download.cancelDownload(id)
        },
        downloader = viewModel.download,
        downloadStatus = mDownloadStatus,
        downloadProgress = mDownloadProgress,
    )
    TrackScreenViewEvent("FeedMoreScreen")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedMoreScreen(
    title: String?,
    feed: ImmutableList<Song>,
    modifier: Modifier = Modifier,
    onClickSongHolder: (List<Song>, Int) -> Unit,
    onClickAddToQueue: (Song) -> Unit,
    onClickDownload: (Song) -> Unit,
    onClickPause: () -> Unit,
    downloader: PodcastDownloader,
    onClickCancelDownload: (Song) -> Unit,
    downloadStatus: SnapshotStateMap<Long, Int>,
    downloadProgress: SnapshotStateMap<Long, Float>,
    onTerminate: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val state = rememberTopAppBarState()
    val behavior = TopAppBarDefaults.pinnedScrollBehavior(state)
    var playActiveId by remember { mutableStateOf<Long?>(null) }
    Scaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            KanadeTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = title ?: "",
                behavior = behavior,
                onTerminate = onTerminate,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            contentPadding = contentPadding,
        ) {
            itemsIndexed(
                items = feed,
                key = { _, song -> song.id },
            ) { index, song ->
                PodcastItemHolder(
                    modifier = Modifier.fillMaxWidth(),
                    song = song,
                    onClickPlayButton = {
                        if (playActiveId == null || playActiveId != song.id) {
                            onClickSongHolder.invoke(feed, index)
                            if (playActiveId != song.id) {
                                playActiveId = song.id
                            }
                        } else {
                            onClickPause.invoke()
                            playActiveId = null
                        }
                    },
                    onClickMenu = { },
                    onClickDownload = onClickDownload,
                    onClickAddToQueue = onClickAddToQueue,
                    isPlay = playActiveId == song.id,
                    downloader = downloader,
                    onClickCancelDownload = onClickCancelDownload,
                    downloadStatus = downloadStatus,
                    downloadProgress = downloadProgress
                )
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            }
        }

    }
}
