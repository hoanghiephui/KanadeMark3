package caios.android.kanade.core.ui.music

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
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.text.HtmlCompat
import caios.android.kanade.core.design.component.KanadeBackground
import caios.android.kanade.core.design.theme.bold
import caios.android.kanade.core.model.music.Song

@Composable
fun PodcastItemHolder(
    song: Song,
    onClickPlay: () -> Unit,
    onClickMenu: () -> Unit,
    onClickAddToQueue: (Song) -> Unit,
    onClickDownload: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(
        modifier
            .padding(all = 8.dp)) {
        val (artwork, title, artist, add, menu, play, download) = createRefs()



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
                    bottom.linkTo(parent.bottom    )
                }
                .clickable { onClickMenu.invoke() },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.PlayCircleOutline,
                    contentDescription = "Play icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            },
            label = {
                Text(text = "Preview")
            },
            onClick = onClickPlay
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
            onClickDownload = {}
        )
    }
}
