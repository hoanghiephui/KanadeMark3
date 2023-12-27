package caios.android.kanade.core.model.podcast

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PodcastDownload(
    val podcastId: Long,
    val title: String,
    val filePath: String,
    val createdAt: Long
) : Parcelable
