package com.podcast.discover

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.podcast.EntryItem
import caios.android.kanade.core.model.podcast.toFeedModel
import caios.android.kanade.core.ui.music.FeedPodcastHolder
import caios.android.kanade.core.ui.view.FixedWithEdgeSpace
import caios.android.kanade.core.ui.view.KanadeTopAppBar
import caios.android.kanade.core.ui.view.itemsWithEdgeSpace
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun DiscoverMoreRouter(
    feed: ImmutableList<EntryItem>,
    modifier: Modifier = Modifier,
    onClickPodcast: (String) -> Unit,
    onTerminate: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    DiscoverMoreScreen(
        feed, modifier, onClickPodcast, onTerminate, contentPadding
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DiscoverMoreScreen(
    feed: ImmutableList<EntryItem>,
    modifier: Modifier = Modifier,
    onClickPodcast: (String) -> Unit,
    onTerminate: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val state = rememberTopAppBarState()
    val behavior = TopAppBarDefaults.pinnedScrollBehavior(state)
    Scaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            KanadeTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.discover),
                behavior = behavior,
                onTerminate = onTerminate,
            )
        },
    ) { paddingValues ->
        LazyVerticalGrid(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            contentPadding = contentPadding,
            columns = FixedWithEdgeSpace(
                count = 3,
                edgeSpace = 8.dp,
            ),
        ) {
            itemsWithEdgeSpace(
                spanCount = 3,
                items = feed,
                key = { artist -> "added-${artist.id}" },
            ) { artist ->
                FeedPodcastHolder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement(),
                    feed = artist.toFeedModel(),
                    onClickHolder = { artist.id?.attributes?.imId?.let { onClickPodcast.invoke(it) } },
                )
            }
        }
    }
}