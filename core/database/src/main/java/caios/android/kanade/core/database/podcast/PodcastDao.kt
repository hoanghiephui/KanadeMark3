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

    @Delete
    fun delete(podcastItem: PodcastEntity)

    @Query("SELECT * FROM podcast_download ORDER BY id ASC")
    fun getAllItems(): LiveData<List<PodcastEntity>>

    @Transaction
    @Query("SELECT * FROM podcast_download WHERE podcast_id = :podcastId")
    fun getItemById(podcastId: Long): PodcastEntity?
}
