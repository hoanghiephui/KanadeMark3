package caios.android.kanade.feature.home.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import caios.android.kanade.core.database.podcast.PodcastModel
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.component.dashedBorder
import caios.android.kanade.core.design.theme.bold
import caios.android.kanade.core.repository.toFeedModel
import caios.android.kanade.core.ui.music.FeedPodcastHolder
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun HomeRecentlySubscribedFeedsSection(
    feeds: ImmutableList<PodcastModel>,
    onClickMore: () -> Unit,
    onClickFeed: (imId: String) -> Unit,
    modifier: Modifier = Modifier,
    onClickAddPodcast: () -> Unit
) {
    Column(
        modifier = modifier.padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.home_title_recently_subscribed),
                style = MaterialTheme.typography.titleMedium.bold(),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (feeds.size >= 3) {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { onClickMore.invoke() }
                        .padding(4.dp),
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 10.dp),
        ) {
            items(
                items = feeds,
                key = { "added-${it.podcastFeed.id}" },
            ) { feed ->
                FeedPodcastHolder(
                    modifier = Modifier.width(120.dp),
                    feed = feed.toFeedModel(),
                    onClickHolder = {
                        onClickFeed.invoke(feed.podcastFeed.id.toString())
                    },
                )
            }
            if (feeds.size <= 2) {
                item {
                    Box(
                        Modifier
                            .size(120.dp)
                            .padding(6.dp)
                            .dashedBorder(1.dp, MaterialTheme.colorScheme.primary, 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            onClickAddPodcast.invoke()
                        }) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}
