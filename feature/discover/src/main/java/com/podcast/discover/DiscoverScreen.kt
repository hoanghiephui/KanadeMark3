package com.podcast.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.theme.bold
import caios.android.kanade.core.model.podcast.Advanced
import caios.android.kanade.core.model.podcast.EntryItem
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.core.ui.TrackScreenViewEvent
import caios.android.kanade.core.ui.collectAsStateLifecycleAware
import com.podcast.discover.items.DiscoverFeedSection

@Composable
internal fun DiscoverRouter(
    topMargin: Dp,
    modifier: Modifier = Modifier,
    viewModel: DiscoverViewModel = hiltViewModel(),
    navigateToFeedDetail: (String) -> Unit,
    navigateToFeedMore: (List<EntryItem>) -> Unit,
    navSearchWith: (id: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateLifecycleAware()
    AsyncLoadContents(
        modifier = modifier,
        screenState = uiState,
    ) { discoverUiState ->
        DiscoverScreen(
            contentPadding = PaddingValues(top = topMargin),
            uiState = discoverUiState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            navigateToFeedDetail = navigateToFeedDetail,
            navigateToFeedMore = navigateToFeedMore,
            itemsAdvance = viewModel.itemsAdvanced,
            navSearchWith = navSearchWith
        )
    }

    TrackScreenViewEvent("DiscoverScreen")
}

@Composable
internal fun DiscoverScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    uiState: Discover,
    itemsAdvance: List<Advanced>,
    navigateToFeedDetail: (String) -> Unit,
    navigateToFeedMore: (List<EntryItem>) -> Unit,
    navSearchWith: (id: Int) -> Unit
) {

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        item {
            DiscoverFeedSection(
                modifier = Modifier.fillMaxWidth(),
                feed = uiState.items,
                onClickMore = {
                    navigateToFeedMore(it.toList())
                },
                onClickPodcast = navigateToFeedDetail,
            )
        }
        item {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.advanced),
                style = MaterialTheme.typography.titleMedium.bold(),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(itemsAdvance) {
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .clickable {
                        navSearchWith.invoke(it.id)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.padding(
                        start = 16.dp,
                    ), painter = painterResource(it.icon), contentDescription = it.title
                )
                Text(text = it.title, modifier = Modifier.padding(start = 4.dp, end = 16.dp))
            }
        }
    }

}
