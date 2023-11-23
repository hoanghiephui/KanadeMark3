package caios.android.kanade.feature.home

import caios.android.kanade.core.model.podcast.EntryItem
import caios.android.kanade.core.ui.error.ImmutableList

sealed interface DiscoverUiState {

    object None : DiscoverUiState

    object Loading : DiscoverUiState

    object Retry : DiscoverUiState

    data class Discover(
        val items: ImmutableList<EntryItem>
    ) : DiscoverUiState
}
