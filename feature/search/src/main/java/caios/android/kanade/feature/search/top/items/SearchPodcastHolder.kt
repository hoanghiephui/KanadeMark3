package caios.android.kanade.feature.search.top.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import caios.android.kanade.core.design.component.KanadeBackground
import caios.android.kanade.core.model.music.Artwork
import caios.android.kanade.core.model.podcast.PodcastSearchResult
import caios.android.kanade.core.model.podcast.dummy
import caios.android.kanade.core.ui.music.Artwork
import caios.android.kanade.feature.search.top.util.getAnnotatedString
import java.util.Locale

@Composable
fun SearchPodcastHolder(
    podcastSearchResult: PodcastSearchResult,
    range: IntRange,
    onClickHolder: () -> Unit,
    onClickMenu: (PodcastSearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(modifier.clickable { onClickHolder.invoke() }) {
        val (artwork, title, artist, duration, menu) = createRefs()

        createVerticalChain(
            title.withChainParams(bottomMargin = 2.dp),
            artist.withChainParams(topMargin = 2.dp),
            chainStyle = ChainStyle.Packed,
        )

        Card(
            modifier = Modifier
                .size(48.dp)
                .constrainAs(artwork) {
                    top.linkTo(parent.top, 12.dp)
                    bottom.linkTo(parent.bottom, 12.dp)
                    start.linkTo(parent.start, 16.dp)
                },
            shape = RoundedCornerShape(8.dp),
        ) {
            Artwork(
                modifier = Modifier.fillMaxSize(),
                artwork = Artwork.Web(podcastSearchResult.imageUrl)
            )
        }

        Text(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(artwork.top)
                start.linkTo(artwork.end, 16.dp)
                end.linkTo(menu.start, 8.dp)

                width = Dimension.fillToConstraints
            },
            text = getAnnotatedString(podcastSearchResult.title, range),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            modifier = Modifier.constrainAs(artist) {
                top.linkTo(title.bottom)
                start.linkTo(title.start)
                end.linkTo(duration.start, 16.dp)

                width = Dimension.fillToConstraints
            },
            text = podcastSearchResult.author,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            modifier = Modifier.constrainAs(duration) {
                top.linkTo(artist.top)
                bottom.linkTo(artist.bottom)
                end.linkTo(menu.start, 8.dp)
            },
            text = podcastSearchResult.trackCount.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Icon(
            modifier = Modifier
                .size(32.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(50))
                .constrainAs(menu) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, 12.dp)
                }
                .clickable { onClickMenu.invoke(podcastSearchResult) }
                .padding(4.dp),
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun SearchSongHolderPreview() {
    KanadeBackground(Modifier.background(MaterialTheme.colorScheme.surface)) {
        SearchPodcastHolder(
            modifier = Modifier.fillMaxWidth(),
            podcastSearchResult = dummy(),
            range = 4..5,
            onClickMenu = {},
            onClickHolder = {},
        )
    }
}
