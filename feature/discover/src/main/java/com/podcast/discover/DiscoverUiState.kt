package com.podcast.discover

import androidx.compose.runtime.Stable
import caios.android.kanade.core.model.podcast.EntryItem
import kotlinx.collections.immutable.ImmutableList

@Stable
data class Discover(
    val items: ImmutableList<EntryItem>
)
