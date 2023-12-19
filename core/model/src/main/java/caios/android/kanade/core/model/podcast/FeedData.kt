package caios.android.kanade.core.model.podcast

import com.prof18.rssparser.model.RssChannel

data class FeedData(
    val resultsItem: ResultsItem,
    val rssChannel: RssChannel
)
