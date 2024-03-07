package com.podcast.discover.detail

import android.app.Activity
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caios.android.kanade.core.common.network.extension.shouldAllowPermission
import caios.android.kanade.core.design.component.AdType
import caios.android.kanade.core.design.component.AdViewState
import caios.android.kanade.core.design.component.MaxTemplateNativeAdViewComposable
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.core.ui.BuildConfig
import caios.android.kanade.core.ui.TrackScreenViewEvent
import caios.android.kanade.core.ui.music.EpisodeDetailHeader
import caios.android.kanade.core.ui.music.PodcastItemHolder
import caios.android.kanade.core.ui.view.CoordinatorData
import caios.android.kanade.core.ui.view.PodcastCoordinatorScaffold
import com.podcast.core.network.util.PodcastDownloader

@Composable
fun OnlineFeedRoute(
    modifier: Modifier = Modifier,
    viewModel: OnlineFeedViewModel = hiltViewModel(),
    terminate: () -> Unit,
    feedId: String,
    feedUrl: String?,
    showSnackBar: (String) -> Unit,
    onClickSeeAll: (List<Song>, String) -> Unit
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    if (feedUrl != null) {
        LaunchedEffect(key1 = feedUrl, block = {
            viewModel.fetchRssDetail(feedUrl, feedId)
        })
    } else {
        LaunchedEffect(key1 = feedId, block = {
            viewModel.getLookFeed(feedId)
        })
    }
    val context = LocalContext.current
    val permission = (context as Activity).shouldAllowPermission()
    val mDownloadStatus: SnapshotStateMap<Long, Int> = remember {
        mutableStateMapOf()
    }
    val mDownloadProgress: SnapshotStateMap<Long, Float> = remember {
        mutableStateMapOf()
    }

    val adState by viewModel.adState
    LaunchedEffect(key1 = BuildConfig.PODCAST_DETAIL_NATIVE, block = {
        viewModel.loadAds(
            context, BuildConfig.PODCAST_DETAIL_NATIVE
        )
    })
    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = {
            if (feedUrl != null) {
                terminate.invoke()
            } else {
                viewModel.getLookFeed(feedId)
            }
        },
    ) { artist ->

        OnlineFeedScreen(
            modifier = Modifier.fillMaxSize(),
            artist = artist,
            onClickMenu = {},
            onTerminate = terminate,
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
            clickSubscribe = {
                if (!it) {
                    viewModel.onSubscribePodcast(imId = feedId, artist = artist)
                    showSnackBar.invoke("Subscribed")
                } else {
                    viewModel.onUnSubscribePodcast(imId = artist.artistId)
                    showSnackBar.invoke("Unsubscribe")
                }
            },
            onClickSeeAll = onClickSeeAll,
            adViewState = adState,
            openBilling = {

            },
            onClickShuffle = viewModel::onShufflePlay
        )
    }

    TrackScreenViewEvent("OnlineFeedScreen")
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
    onClickPause: () -> Unit,
    downloader: PodcastDownloader,
    onClickCancelDownload: (Song) -> Unit,
    downloadStatus: SnapshotStateMap<Long, Int>,
    downloadProgress: SnapshotStateMap<Long, Float>,
    clickSubscribe: (Boolean) -> Unit,
    onClickSeeAll: (List<Song>, String) -> Unit,
    adViewState: AdViewState,
    openBilling: () -> Unit,
    onClickShuffle: (List<Song>) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    var textLine by remember {
        mutableStateOf(false)
    }
    val coordinatorData = remember {
        CoordinatorData.Podcast(
            title = artist.artist,
            summary = artist.description ?: "",
            artwork = artist.artwork,
            author = artist.author ?: "",
            isSubscribe = artist.isSubscribe
        )
    }
    var playActiveId by remember { mutableStateOf<Long?>(null) }
    PodcastCoordinatorScaffold(
        modifier = modifier.fillMaxSize(),
        data = coordinatorData,
        onClickNavigateUp = onTerminate,
        onClickMenu = { onClickMenu.invoke(artist) },
        clickSubscribe = clickSubscribe,
        isSubscribe = artist.isSubscribe,
        clickShuffle = {
            onClickShuffle.invoke(artist.songs)
        }
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
                style = MaterialTheme.typography.bodyMedium,
                onTextLayout = {
                    textLine = it.lineCount >= 3
                }
            )
            if (textLine) {
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

        }

        item {
            MaxTemplateNativeAdViewComposable(
                adViewState = adViewState,
                adType = AdType.SMALL,
                showBilling = openBilling,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            EpisodeDetailHeader(
                modifier = Modifier.fillMaxWidth(),
                onClickSeeAll = {
                    onClickSeeAll.invoke(artist.songs, artist.artist)
                },
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
                onClickPlayButton = {
                    if (playActiveId == null || playActiveId != song.id) {
                        onClickSongHolder.invoke(artist.songs, index)
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
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp, start = 12.dp))
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
