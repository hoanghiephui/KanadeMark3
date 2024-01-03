package caios.android.kanade.core.model.podcast

data class PodcastSearchResult(
    val id: Int,
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
    val author: String,
    val trackCount: Int
)

fun dummy(id: Int = 0): PodcastSearchResult {
    return PodcastSearchResult(
        id = id,
        title = "サンプル楽曲$id",
        author = "CAIOS",
        trackCount = 2,
        imageUrl = "Song",
        feedUrl = "",
    )
}
