package com.podcast.discover

import androidx.compose.runtime.Stable
import caios.android.kanade.core.model.podcast.Advanced
import caios.android.kanade.core.model.podcast.EntryItem
import kotlinx.collections.immutable.ImmutableList

@Stable
data class Discover(
    val topFeed: ImmutableList<EntryItem>,
    val healthFeed: ImmutableList<EntryItem>,
    val educationFeed: ImmutableList<EntryItem>,
    val musicFeed: ImmutableList<EntryItem>,
    val societyFeed: ImmutableList<EntryItem>,
    val itemsAdvanced: ImmutableList<Advanced>
)
