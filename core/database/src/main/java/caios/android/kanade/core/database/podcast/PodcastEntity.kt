package caios.android.kanade.core.database.podcast

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.File
import java.io.IOException
import java.text.CharacterIterator
import java.text.DateFormat
import java.text.StringCharacterIterator
import java.util.Date
import java.util.Locale

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
