package com.podcast.core.network.util

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.webkit.URLUtil
import caios.android.kanade.core.common.network.util.generateFileName
import caios.android.kanade.core.model.music.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils

class PodcastDownloader(context: Context) {
    companion object {
        const val DOWNLOAD_DIR = "PodCast"
        val FILE_FOLDER_PATH =
            "/storage/emulated/0/${Environment.DIRECTORY_DOWNLOADS}/${DOWNLOAD_DIR}"
    }

    private val downloadJob = Job()
    private val downloadScope = CoroutineScope(Dispatchers.IO + downloadJob)
    private val downloadManager by lazy { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    data class DownloadInfo(
        val downloadId: Long,
        var status: Int = DownloadManager.STATUS_RUNNING,
        val progress: MutableStateFlow<Float> = MutableStateFlow(0f)
    )

    /** Stores running download with Song id as key */
    private val runningDownloads = HashMap<Long, DownloadInfo>()

    /**
     * Start downloading epub file for the given [Song] object.
     * @param song [Song] which needs to be downloaded.
     * @param downloadProgressListener a callable which takes download progress; [Float] and
     * download status; [Int] as arguments.
     * @param onDownloadSuccess: a callable which will be executed after download has been
     * completed successfully.
     */
    @SuppressLint("Range")
    fun downloadBook(
        song: Song, downloadProgressListener: (Float, Int) -> Unit, onDownloadSuccess: () -> Unit
    ) {
        val filename = getMediaFileName(song)
        val uri = song.uri
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverRoaming(true).setAllowedOverMetered(true).setTitle(song.title)
            .setDescription(song.title)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, "$DOWNLOAD_DIR/$filename"
            )
        val downloadId = downloadManager.enqueue(request)

        downloadScope.launch {
            var isDownloadFinished = false
            var progress = 0f
            var status: Int
            runningDownloads[song.id] = DownloadInfo(downloadId)

            while (!isDownloadFinished) {
                val cursor: Cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor.moveToFirst()) {
                    status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_RUNNING -> {
                            val totalBytes: Long =
                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            if (totalBytes > 0) {
                                val downloadedBytes: Long =
                                    cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                progress = (downloadedBytes * 100 / totalBytes).toFloat() / 100
                            }
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            isDownloadFinished = true
                            progress = 1f
                            onDownloadSuccess()
                        }

                        DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {}
                        DownloadManager.STATUS_FAILED -> {
                            isDownloadFinished = true
                            progress = 0f
                        }
                    }

                } else {
                    /** Download cancelled by the user. */
                    isDownloadFinished = true
                    progress = 0f
                    status = DownloadManager.STATUS_FAILED
                }

                /** update download info at the end of iteration. */
                runningDownloads[song.id]?.status = status
                downloadProgressListener(progress, status)
                runningDownloads[song.id]?.progress?.value = progress
                cursor.close()
            }
            /**
            Remove download from running downloads when loop ends.
            added dome delay here so we get time to update our UI before
            download info gets removed.
             */
            delay(500L)
            runningDownloads.remove(song.id)
        }
    }

    /**
     * Returns true if Song with the given id is currently being downloaded
     * false otherwise.
     */
    fun isPodcastCurrentlyDownloading(podcastId: Long) = runningDownloads.containsKey(podcastId)

    /**
     * Returns [DownloadInfo] for the given Song id if it's currently
     * being downloaded, null otherwise.
     */
    fun getRunningDownload(podcastId: Long) = runningDownloads[podcastId]

    /**
     * Cancels download of Song by using it's download id (if download is running).
     */
    fun cancelDownload(downloadId: Long?) = downloadId?.let { downloadManager.remove(it) }

    fun getMediaFileName(media: Song): String {
        val titleBaseFilename: String

        // Try to generate the filename by the item title
        val title: String = media.title
        titleBaseFilename = generateFileName(title)
        val urlBaseFilename =
            URLUtil.guessFileName(media.data, null, media.mimeType)
        var baseFilename: String
        baseFilename = if (titleBaseFilename != "") {
            titleBaseFilename
        } else {
            urlBaseFilename
        }
        val filenameMaxLength = 220
        if (baseFilename.length > filenameMaxLength) {
            baseFilename = baseFilename.substring(0, filenameMaxLength)
        }
        return (baseFilename + FilenameUtils.EXTENSION_SEPARATOR + media.id
                + FilenameUtils.EXTENSION_SEPARATOR) + FilenameUtils.getExtension(
            urlBaseFilename
        )
    }
}
