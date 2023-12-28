package caios.android.kanade.core.database.podcast

import androidx.room.Embedded
import androidx.room.Relation

class PodcastModel {
    @Embedded
    lateinit var podcastFeed: PodcastFeedEntity

    @Relation(
        parentColumn = "id",
        entityColumn = "id_podcast",
    )
    lateinit var items: List<PodcastFeedItemEntity>
}
