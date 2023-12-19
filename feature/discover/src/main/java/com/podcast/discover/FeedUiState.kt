package com.podcast.discover

import androidx.compose.runtime.Stable
import caios.android.kanade.core.model.podcast.EntryItem
import com.prof18.rssparser.model.RssChannel
import kotlinx.collections.immutable.ImmutableList

@Stable
data class FeedPodcast(
    val item: RssChannel
)
