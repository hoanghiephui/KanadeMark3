package caios.android.kanade.core.database.podcast

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PodcastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(podcastItem: PodcastEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPodcastFeed(entity: PodcastFeedEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPodcastFeedItem(vararg entity: PodcastFeedItemEntity)

    @Transaction
    @Query("DELETE FROM podcast_feed WHERE id = :podcastId")
    fun delete(podcastId: Long)

    @Transaction
    @Query("DELETE FROM podcast_feed_item WHERE id = :podcastItemId")
    fun deleteItem(podcastItemId: Long)

    @Transaction
    @Query("SELECT * FROM podcast_feed")
    fun loadAll(): List<PodcastModel>

    @Transaction
    @Query("SELECT * FROM podcast_feed WHERE id = :podcastId")
    fun load(podcastId: Long): PodcastModel?

    @Transaction
    @Query("SELECT * FROM podcast_feed_item WHERE id = :podcastItemId")
    fun loadItem(podcastItemId: Long): PodcastFeedItemEntity

    @Delete
    fun delete(podcastItem: PodcastEntity)

    @Query("SELECT * FROM podcast_download ORDER BY id ASC")
    fun getAllItems(): LiveData<List<PodcastEntity>>

    @Transaction
    @Query("SELECT * FROM podcast_download WHERE podcast_id = :podcastId")
    fun getItemById(podcastId: Long): PodcastEntity?
}
