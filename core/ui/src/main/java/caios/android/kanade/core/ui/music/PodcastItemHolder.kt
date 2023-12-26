package caios.android.kanade.core.ui.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ChipColors
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import caios.android.kanade.core.design.component.KanadeBackground
import caios.android.kanade.core.design.theme.bold
import caios.android.kanade.core.model.music.Song

@Composable
fun PodcastItemHolder(
    song: Song,
    onClickPlay: () -> Unit,
    onClickPause: () -> Unit,
    onClickMenu: () -> Unit,
    onClickAddToQueue: (Song) -> Unit,
    onClickDownload: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(
        modifier
            .padding(all = 8.dp)
    ) {
        val (artwork, title, artist, add, menu, play, download) = createRefs()

        var isPlayActive by remember { mutableStateOf(false) }

        Artwork(
            modifier = Modifier
                .size(64.dp)
                .aspectRatio(1f)
                .constrainAs(artwork) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                },
            artwork = song.albumArtwork,
        )

        Text(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(artwork.top)
                start.linkTo(artwork.end, 16.dp)
                end.linkTo(parent.end, 16.dp)

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
                start.linkTo(artwork.end, 16.dp)
                end.linkTo(parent.end, 16.dp)
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
                    end.linkTo(parent.end, 16.dp)
                }
                .clickable { onClickMenu.invoke() }
                .padding(4.dp),
            imageVector = Icons.Default.MoreVert,
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
                Icon(
                    imageVector = Icons.Outlined.PlayCircleOutline,
                    contentDescription = "Play icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            },
            label = {
                val text = if (isPlayActive) "Playing" else "Preview"
                Text(text = text)
            },
            onClick = {
                if (!isPlayActive) {
                    onClickPlay.invoke()
                } else {
                    onClickPause.invoke()
                }
                isPlayActive = !isPlayActive
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

        Icon(
            modifier = Modifier
                .size(32.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(50))
                .constrainAs(download) {
                    top.linkTo(add.top)
                    bottom.linkTo(add.bottom)
                    start.linkTo(add.end, 16.dp)
                }
                .clickable { onClickDownload.invoke(song) }
                .padding(6.dp),
            imageVector = Icons.Outlined.ArrowCircleDown,
            contentDescription = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MusicHolderPreview() {
    KanadeBackground(Modifier.wrapContentSize()) {
        PodcastItemHolder(
            modifier = Modifier.fillMaxWidth(),
            song = Song.dummy(),
            onClickPlay = { },
            onClickMenu = { },
            onClickAddToQueue = {},
            onClickDownload = {},
            onClickPause = {}
        )
    }
}
