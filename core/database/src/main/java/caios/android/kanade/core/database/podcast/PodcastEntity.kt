package caios.android.kanade.core.database.podcast

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import java.io.File
import java.io.IOException
import java.text.CharacterIterator
import java.text.DateFormat
import java.text.StringCharacterIterator
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "podcast_feed",
    indices = [Index(value = ["id"], unique = true)]
)
data class PodcastFeedEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Long,
    @ColumnInfo(name = "im_id")
    val imId: String,
    @ColumnInfo(name = "id_podcast")
    val idPodcast: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String? = null,
    @ColumnInfo(name = "author")
    val author: String? = null,
    @ColumnInfo("avatar")
    val urlAvatar: String? = null,
    val timeStamp: Long
)

@Entity(
    tableName = "podcast_feed_item",
    indices = [Index(value = ["id_podcast"])],
    foreignKeys = [
        ForeignKey(
            entity = PodcastFeedEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_podcast"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PodcastFeedItemEntity(
    @ColumnInfo("id_podcast")
    val idPodcast: Long,
    @ColumnInfo("song_id")
    val songId: Long,
    val title: String,
    @ColumnInfo("description")
    val artist: String,
    val duration: Long,
    val year: Int,
    val data: String,
    val dateModified: Long,
    val image: String? = null,
    val publishDate: Instant
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Entity(
    tableName = "podcast_download",
    indices = [Index(value = ["id"], unique = true)],
)
data class PodcastEntity(
    @ColumnInfo(name = "podcast_id")
    val podcastId: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo("filePath")
    val filePath: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    var id: Int = 0
    fun fileExist(): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    fun getFileSize(): String {
        val file = File(filePath)
        var bytes = file.length()
        if (-1000 < bytes && bytes < 1000) {
            return "$bytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        while (bytes <= -999950 || bytes >= 999950) {
            bytes /= 1000
            ci.next()
        }
        return java.lang.String.format(Locale.US, "%.1f %cB", bytes / 1000.0, ci.current())
    }

    fun getDownloadDate(): String {
        val date = Date(createdAt)
        return DateFormat.getDateInstance().format(date)
    }

    fun deleteFile(): Boolean {
        val file = File(filePath)
        return try {
            file.delete()
        } catch (exc: IOException) {
            false
        }
    }
}
