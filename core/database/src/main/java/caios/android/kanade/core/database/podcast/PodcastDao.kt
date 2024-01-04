package caios.android.kanade.core.database.podcast

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(podcastItem: PodcastEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcastFeed(entity: PodcastFeedEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcastFeedItem(entity: List<PodcastFeedItemEntity>)

    @Transaction
    @Query("DELETE FROM podcast_feed WHERE id = :podcastId")
    suspend fun delete(podcastId: Long)

    @Transaction
    @Query("DELETE FROM podcast_feed_item WHERE id = :podcastItemId")
    fun deleteItem(podcastItemId: Long)

    @Transaction
    @Query("SELECT * FROM podcast_feed")
    fun loadAll(): Flow<List<PodcastModel>>

    @Transaction
    @Query("SELECT * FROM podcast_feed ORDER BY timeStamp DESC LIMIT 10")
    fun loadLatestAdd(): Flow<List<PodcastModel>>

    @Transaction
    @Query("SELECT * FROM podcast_feed_item")
    fun loadAddItem(): Flow<List<PodcastFeedItemEntity>>

    @Transaction
    @Query("SELECT * FROM podcast_feed WHERE id = :podcastId")
    suspend fun load(podcastId: Long): PodcastModel?

    @Transaction
    @Query("SELECT * FROM podcast_feed_item WHERE id = :podcastItemId")
    fun loadItem(podcastItemId: Long): PodcastFeedItemEntity?

    @Transaction
    @Query("SELECT * FROM podcast_feed_item WHERE song_id = :songId")
    suspend fun loadItemSong(songId: Long): PodcastFeedItemEntity?

    @Delete
    suspend fun delete(podcastItem: PodcastEntity)

    @Transaction
    @Query("SELECT * FROM podcast_download WHERE podcast_id = :podcastId")
    suspend fun getItemById(podcastId: Long): PodcastEntity?
}
