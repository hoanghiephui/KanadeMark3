package com.podcast.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.theme.bold
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.core.ui.collectAsStateLifecycleAware
import com.podcast.discover.items.DiscoverFeedSection

@Composable
internal fun DiscoverRouter(
    topMargin: Dp,
    modifier: Modifier = Modifier,
    viewModel: DiscoverViewModel = hiltViewModel(),
    navigateToFeedDetail: (String) -> Unit
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
            navigateToFeedDetail = navigateToFeedDetail
        )
    }
}

@Composable
internal fun DiscoverScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    uiState: Discover,
    navigateToFeedDetail: (String) -> Unit
) {

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        item {
            DiscoverFeedSection(
                modifier = Modifier.fillMaxWidth(),
                feed = uiState.items,
                onClickMore = {},
                onClickPodcast = navigateToFeedDetail,
                onClickAlbumPlay = { _, _ -> },
                onClickAlbumMenu = {},
            )
        }
        item {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.advanced),
                style = MaterialTheme.typography.titleMedium.bold(),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

}
