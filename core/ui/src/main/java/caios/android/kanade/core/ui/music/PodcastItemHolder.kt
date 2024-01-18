package caios.android.kanade.core.ui.music

import android.app.DownloadManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.component.KanadeBackground
import caios.android.kanade.core.design.theme.bold
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.ui.view.dateFormatted
import com.podcast.core.network.util.PodcastDownloader
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun PodcastItemHolder(
    song: Song,
    onClickPlayButton: () -> Unit,
    onClickMenu: () -> Unit,
    onClickAddToQueue: (Song) -> Unit,
    onClickDownload: (Song) -> Unit,
    onClickCancelDownload: (Song) -> Unit,
    modifier: Modifier = Modifier,
    isPlay: Boolean,
    downloader: PodcastDownloader,
    downloadStatus: SnapshotStateMap<Long, Int>,
    downloadProgress: SnapshotStateMap<Long, Float>
) {
    // Check if this book is in downloadQueue.
    val buttonIconValue =
        if (downloader.isPodcastCurrentlyDownloading(song.id)) {
            Icons.Filled.Downloading
        } else {
            if (song.isDownloaded) Icons.Filled.DownloadForOffline else Icons.Outlined.ArrowCircleDown
        }

    var buttonIcon by remember { mutableStateOf(buttonIconValue) }
    var progressState by remember { mutableFloatStateOf(0f) }
    var showProgressBar by remember { mutableStateOf(false) }
    // Callable which updates book details screen button.
    val updateBtnIcon: (Int?) -> Unit = { status ->
        buttonIcon = if (song.isDownloaded) Icons.Filled.DownloadForOffline else when (status) {
            DownloadManager.STATUS_RUNNING -> {
                showProgressBar = true
                Icons.Filled.Downloading
            }

            DownloadManager.STATUS_SUCCESSFUL -> {
                showProgressBar = false
                Icons.Filled.DownloadForOffline
            }

            else -> {
                showProgressBar = false
                Icons.Outlined.ArrowCircleDown
            }
        }
    }
    LaunchedEffect(key1 = downloadStatus[song.id], block = {
        updateBtnIcon(downloadStatus[song.id])
    })
    LaunchedEffect(key1 = downloadProgress[song.id], block = {
        progressState = downloadProgress[song.id] ?: 0F
    })

    // Check if this book is in downloadQueue.
    if (downloader.isPodcastCurrentlyDownloading(song.id)) {
        progressState =
            downloader.getRunningDownload(song.id)?.progress?.collectAsState()?.value!!
        LaunchedEffect(key1 = progressState, block = {
            updateBtnIcon(downloader.getRunningDownload(song.id)?.status)
        })
    }

    var atEnd by remember { mutableStateOf(false) }
    val image = AnimatedImageVector.animatedVectorResource(R.drawable.av_equalizer)
    suspend fun runAnimation() {
        while (isPlay) {
            atEnd = !atEnd
            delay(1000)
        }
    }

    LaunchedEffect(isPlay) {
        runAnimation()
    }
    ConstraintLayout(
        modifier
            .padding(all = 12.dp)
    ) {
        val (artwork, title, artist, add, menu, play, download, time) = createRefs()
        val formattedDate = dateFormatted(
            Instant.fromEpochMilliseconds(song.publishDate))
        Text(
            modifier = Modifier.constrainAs(time) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                width = Dimension.fillToConstraints
            },
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Artwork(
            artwork = song.albumArtwork,
            modifier = Modifier
                .size(64.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8))
                .constrainAs(artwork) {
                    top.linkTo(time.bottom, 8.dp)
                    start.linkTo(parent.start)
                }
        )


        Text(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(artwork.top)
                start.linkTo(artwork.end, 8.dp)
                end.linkTo(parent.end, 8.dp)

                width = Dimension.fillToConstraints
            },
            text = song.title,
            style = MaterialTheme.typography.bodyLarge.bold(),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            modifier = Modifier.constrainAs(artist) {
                top.linkTo(title.bottom)
                start.linkTo(artwork.end, 8.dp)
                end.linkTo(parent.end, 8.dp)
                width = Dimension.fillToConstraints
            },
            text = song.artist,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )


        Icon(
            modifier = Modifier
                .size(32.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(50))
                .constrainAs(menu) {
                    top.linkTo(play.top)
                    bottom.linkTo(play.bottom)
                    start.linkTo(download.end, 16.dp)
                }
                .clickable { onClickMenu.invoke() }
                .padding(6.dp),
            imageVector = Icons.Filled.Share,
            contentDescription = null,
        )

        AssistChip(
            modifier = Modifier
                .constrainAs(play) {
                    top.linkTo(artwork.bottom)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                },
            leadingIcon = {
                if (isPlay) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        painter = rememberAnimatedVectorPainter(image, atEnd),
                        contentDescription = "Play icon",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircleOutline,
                        contentDescription = "Play icon",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            },
            label = {
                val text = if (isPlay) "Playing" else "Preview"
                Text(text = text)
            },
            onClick = {
                onClickPlayButton.invoke()
            }
        )

        Icon(
            modifier = Modifier
                .size(32.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(50))
                .constrainAs(add) {
                    top.linkTo(play.top)
                    bottom.linkTo(play.bottom)
                    start.linkTo(play.end, 16.dp)
                }
                .clickable { onClickAddToQueue.invoke(song) }
                .padding(4.dp),
            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
            contentDescription = null,
        )
        Box(modifier = Modifier
            .size(32.dp)
            .aspectRatio(1f)
            .constrainAs(download) {
                top.linkTo(add.top)
                bottom.linkTo(add.bottom)
                start.linkTo(add.end, 16.dp)
            }
        ) {
            val progress by animateFloatAsState(
                targetValue = progressState,
                label = "download progress bar"
            )
            AnimatedVisibility(visible = showProgressBar) {
                if (progressState > 0f) {
                    // Determinate progress bar.
                    CircularProgressIndicator(
                        progress = { progress },
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                } else {
                    // Indeterminate progress bar.
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 3.dp
                    )
                }
            }
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        when (buttonIcon) {
                            Icons.Filled.Downloading -> {
                                onClickCancelDownload.invoke(song)
                            }

                            Icons.Outlined.ArrowCircleDown -> {
                                onClickDownload.invoke(song)
                            }
                        }
                    }
                    .padding(6.dp),
                imageVector = buttonIcon,
                tint = if (song.isDownloaded) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                contentDescription = null,
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun MusicHolderPreview() {
    KanadeBackground(Modifier.wrapContentSize()) {
        PodcastItemHolder(
            modifier = Modifier.fillMaxWidth(),
            song = Song.dummy(),
            onClickPlayButton = { },
            onClickMenu = { },
            onClickAddToQueue = {},
            onClickDownload = {},
            isPlay = false,
            downloader = PodcastDownloader(LocalContext.current),
            onClickCancelDownload = {},
            downloadStatus = mutableStateMapOf(),
            downloadProgress = mutableStateMapOf()
        )
    }
}
