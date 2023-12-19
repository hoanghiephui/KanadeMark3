package com.podcast.discover

import caios.android.kanade.core.model.podcast.EntryItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class DiscoverFeedItemMapper @Inject constructor() {

    fun map(movies: List<EntryItem>): ImmutableList<EntryItem> =
        movies.map(::map).toImmutableList()

    private fun map(movie: EntryItem): EntryItem =
        movie
}
