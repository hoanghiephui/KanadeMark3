package com.podcast.episodes

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caios.android.kanade.core.common.network.extension.shouldAllowPermission
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.component.AdType
import caios.android.kanade.core.design.component.AdViewState
import caios.android.kanade.core.design.component.MaxTemplateNativeAdViewComposable
import caios.android.kanade.core.design.theme.end
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.core.ui.BuildConfig
import caios.android.kanade.core.ui.TrackScreenViewEvent
import caios.android.kanade.core.ui.music.PodcastItemHolder
import caios.android.kanade.core.ui.view.EmptyView
import com.podcast.core.network.util.PodcastDownloader
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.reflect.KClass

@Composable
fun EpisodesRouter(
    topMargin: Dp,
    navigateToSongMenu: (Song) -> Unit,
    navigateToSort: (KClass<*>) -> Unit,
    modifier: Modifier = Modifier,
    showSnackBar: (String) -> Unit,
    viewModel: EpisodesViewModel = hiltViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permission = (context as Activity).shouldAllowPermission()
    val mDownloadStatus: SnapshotStateMap<Long, Int> = remember {
        mutableStateMapOf()
    }
    val mDownloadProgress: SnapshotStateMap<Long, Float> = remember {
        mutableStateMapOf()
    }
    val adState by viewModel.adState
    LaunchedEffect(key1 = BuildConfig.EPISODES_NATIVE, block = {
        viewModel.loadAds(
            context, BuildConfig.EPISODES_NATIVE
        )
    })
    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) { uiState ->
        if (uiState.isEmpty()) {
            EmptyView(
                title = R.string.no_episodes,
                content = R.string.no_episodes_content,
                adViewState = adState,
                openBilling = {}
            )
        } else {
            EpisodesScreen(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                songs = uiState.toImmutableList(),
                contentPadding = PaddingValues(top = topMargin + 8.dp),
                onClickSong = viewModel::onNewPlay,
                onClickMenu = navigateToSongMenu,
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
                adViewState = adState,
                openBilling = {

                }
            )
        }
    }

    TrackScreenViewEvent("EpisodesScreen")
}

@Composable
private fun EpisodesScreen(
    songs: ImmutableList<Song>,
    onClickSong: (Int, List<Song>) -> Unit,
    onClickMenu: (Song) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onClickAddToQueue: (Song) -> Unit,
    onClickDownload: (Song) -> Unit,
    onClickPause: () -> Unit,
    downloader: PodcastDownloader,
    onClickCancelDownload: (Song) -> Unit,
    downloadStatus: SnapshotStateMap<Long, Int>,
    downloadProgress: SnapshotStateMap<Long, Float>,
    adViewState: AdViewState,
    openBilling: () -> Unit
) {
    var playActiveId by remember { mutableStateOf<Long?>(null) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item {
            MaxTemplateNativeAdViewComposable(
                adViewState = adViewState,
                adType = AdType.SMALL,
                showBilling = openBilling,
                modifier = Modifier.padding(
                    vertical = 8.dp
                )
            )
        }
        item {
            Row(
                modifier = modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(1f),
                    text = stringResource(R.string.unit_episodes, songs.size),
                    style = MaterialTheme.typography.bodyMedium.end(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

        }
        itemsIndexed(
            items = songs,
            key = { _, song -> song.id },
        ) { index, song ->
            PodcastItemHolder(
                modifier = Modifier.fillMaxWidth(),
                song = song,
                onClickPlayButton = {
                    if (playActiveId == null || playActiveId != song.id) {
                        onClickSong.invoke(index, songs)
                        if (playActiveId != song.id) {
                            playActiveId = song.id
                        }
                    } else {
                        onClickPause.invoke()
                        playActiveId = null
                    }
                },
                onClickMenu = { onClickMenu.invoke(song) },
                onClickDownload = onClickDownload,
                onClickAddToQueue = onClickAddToQueue,
                isPlay = playActiveId == song.id,
                downloader = downloader,
                onClickCancelDownload = onClickCancelDownload,
                downloadStatus = downloadStatus,
                downloadProgress = downloadProgress
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp, start = 12.dp))
        }
    }
}
