package caios.android.kanade.core.model.podcast

data class PodcastSearchResult(
    /**
     * The name of the podcast
     */
    val title: String,
    /**
     * URL of the podcast image
     */
    val imageUrl: String,
    /**
     * URL of the podcast feed
     */
    val feedUrl: String,
    /**
     * artistName of the podcast feed
     */
    val author: String
)
