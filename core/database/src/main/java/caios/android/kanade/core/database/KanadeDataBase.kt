package caios.android.kanade.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import caios.android.kanade.core.database.album_detail.AlbumDetailDao
import caios.android.kanade.core.database.album_detail.AlbumDetailEntity
import caios.android.kanade.core.database.album_detail.AlbumTagEntity
import caios.android.kanade.core.database.album_detail.AlbumTrackEntity
import caios.android.kanade.core.database.artist_detail.ArtistDetailDao
import caios.android.kanade.core.database.artist_detail.ArtistDetailEntity
import caios.android.kanade.core.database.artist_detail.ArtistTagEntity
import caios.android.kanade.core.database.artist_detail.SimilarArtistEntity
import caios.android.kanade.core.database.artwork.ArtworkDao
import caios.android.kanade.core.database.artwork.ArtworkEntity
import caios.android.kanade.core.database.history.PlayHistoryDao
import caios.android.kanade.core.database.history.PlayHistoryEntity
import caios.android.kanade.core.database.playlist.PlaylistDao
import caios.android.kanade.core.database.playlist.PlaylistEntity
import caios.android.kanade.core.database.playlist.PlaylistItemEntity
import caios.android.kanade.core.database.podcast.PodcastDao
import caios.android.kanade.core.database.podcast.PodcastEntity
import caios.android.kanade.core.database.podcast.PodcastFeedEntity
import caios.android.kanade.core.database.podcast.PodcastFeedItemEntity

@Database(
    entities = [
        ArtistDetailEntity::class,
        ArtistTagEntity::class,
        SimilarArtistEntity::class,
        AlbumDetailEntity::class,
        AlbumTrackEntity::class,
        AlbumTagEntity::class,
        ArtworkEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        PlayHistoryEntity::class,
        PodcastEntity::class,
        PodcastFeedItemEntity::class,
        PodcastFeedEntity::class
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(1, 2),
        AutoMigration(2, 3)
    ],
    exportSchema = true,
)
@TypeConverters(
    InstantConverter::class,
)
abstract class KanadeDataBase : RoomDatabase() {
    abstract fun artistDetailDao(): ArtistDetailDao
    abstract fun albumDetailDao(): AlbumDetailDao
    abstract fun artworkDao(): ArtworkDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun podcastDao(): PodcastDao
}
