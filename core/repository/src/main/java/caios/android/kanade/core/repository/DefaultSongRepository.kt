package caios.android.kanade.core.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Audio.Media
import caios.android.kanade.core.model.MusicConfig
import caios.android.kanade.core.model.MusicOrder
import caios.android.kanade.core.model.Song
import caios.android.kanade.core.repository.util.getInt
import caios.android.kanade.core.repository.util.getLong
import caios.android.kanade.core.repository.util.getString
import caios.android.kanade.core.repository.util.getStringOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class DefaultSongRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SongRepository {

    private val baseProjection = arrayOf(
        BaseColumns._ID,
        AudioColumns.TITLE,
        AudioColumns.TRACK,
        AudioColumns.YEAR,
        AudioColumns.DURATION,
        Media.DATA,
        AudioColumns.DATE_MODIFIED,
        AudioColumns.ALBUM_ID,
        AudioColumns.ALBUM,
        AudioColumns.ARTIST_ID,
        AudioColumns.ARTIST,
        AudioColumns.MIME_TYPE,
    )

    override fun song(songId: Long, musicConfig: MusicConfig): Song? {
        return song(
            makeCursor(
                selection = AudioColumns._ID,
                selectionValues = listOf(songId.toString()),
                musicOrders = arrayOf(musicConfig.songOrder),
            ),
        )
    }

    override fun song(cursor: Cursor?): Song? {
        val song = if (cursor != null && cursor.moveToFirst()) getSong(cursor) else null
        cursor?.close()
        return song
    }

    override fun songs(musicConfig: MusicConfig): List<Song> {
        return songs(makeCursor(musicOrders = arrayOf(musicConfig.songOrder)))
    }

    override fun songs(cursor: Cursor?): List<Song> {
        val songs = mutableListOf<Song>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSong(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        Timber.d("songs: ${songs.size}")
        return songs
    }

    override fun makeCursor(
        selection: String,
        selectionValues: List<String>,
        vararg musicOrders: MusicOrder,
    ): Cursor? {
        val order = musicOrders.joinToString(separator = ", ") { it.create() }
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else Media.EXTERNAL_CONTENT_URI

        var selectionFinal = AudioColumns.IS_MUSIC + "=1" + " AND " + AudioColumns.TITLE + " != ''"
        var selectionValuesFinal = emptyArray<String>()

        if (selection.isNotBlank()) {
            selectionFinal += " AND $selection"
            selectionValuesFinal += selectionValues
        }

        selectionFinal += " AND ${Media.DURATION} >= 5000"

        return try {
            context.contentResolver.query(
                uri,
                baseProjection,
                selectionFinal,
                selectionValuesFinal,
                order,
            )
        } catch (ex: SecurityException) {
            return null
        }
    }

    private fun getSong(cursor: Cursor): Song {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else Media.EXTERNAL_CONTENT_URI

        val id = cursor.getLong(AudioColumns._ID)
        val title = cursor.getString(AudioColumns.TITLE)
        val trackNumber = cursor.getInt(AudioColumns.TRACK)
        val year = cursor.getInt(AudioColumns.YEAR)
        val duration = cursor.getLong(AudioColumns.DURATION)
        val data = cursor.getString(AudioColumns.DATA)
        val dateModified = cursor.getLong(AudioColumns.DATE_MODIFIED)
        val albumId = cursor.getLong(AudioColumns.ALBUM_ID)
        val albumName = cursor.getStringOrNull(AudioColumns.ALBUM)
        val artistId = cursor.getLong(AudioColumns.ARTIST_ID)
        val artistName = cursor.getStringOrNull(AudioColumns.ARTIST)
        val mimeType = cursor.getString(AudioColumns.MIME_TYPE)

        return Song(
            id = id,
            title = title,
            artist = artistName ?: "",
            artistId = artistId,
            album = albumName ?: "",
            albumId = albumId,
            duration = duration,
            year = year,
            track = trackNumber,
            mimeType = mimeType,
            data = data,
            dateModified = dateModified,
            uri = Uri.withAppendedPath(uri, id.toString()),
        )
    }
}
