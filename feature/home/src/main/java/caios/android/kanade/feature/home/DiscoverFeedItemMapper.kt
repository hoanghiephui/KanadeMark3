package caios.android.kanade.feature.home

import caios.android.kanade.core.model.podcast.EntryItem
import caios.android.kanade.core.ui.error.ImmutableList
import javax.inject.Inject

class DiscoverFeedItemMapper @Inject constructor() {

    fun map(movies: List<EntryItem>): ImmutableList<EntryItem> =
        ImmutableList(movies.mapNotNull(::map))

    private fun map(movie: EntryItem): EntryItem? =
        movie
}
