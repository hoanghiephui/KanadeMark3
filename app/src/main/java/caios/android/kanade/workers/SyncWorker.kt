package caios.android.kanade.workers

import android.content.Context
import androidx.core.text.HtmlCompat
import androidx.hilt.work.HiltWorker
import androidx.media3.common.MimeTypes
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.common.network.data
import caios.android.kanade.core.common.network.util.DateUtils
import caios.android.kanade.core.common.network.util.inMillis
import caios.android.kanade.core.database.podcast.PodcastFeedItemEntity
import caios.android.kanade.core.repository.podcast.ParseRssRepository
import caios.android.kanade.core.repository.podcast.UpdatePodcastRepository
import caios.android.kanade.core.repository.podcast.getRssChannel
import com.podcast.analytic.AnalyticsHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinTimeZone
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.math.BigInteger
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val analyticsHelper: AnalyticsHelper,
    @Dispatcher(KanadeDispatcher.IO)
    private val ioDispatcher: CoroutineDispatcher,
    private val updatePodcastRepository: UpdatePodcastRepository,
    private val parseRssRepository: ParseRssRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun getForegroundInfo(): ForegroundInfo =
        appContext.syncForegroundInfo()

    override suspend fun doWork() = withContext(ioDispatcher) {
        analyticsHelper.logSyncStarted()
        val data =
            updatePodcastRepository.loadAddPodcast().map {
                PodcastData(
                    it.podcastFeed.podcastSource,
                    it.podcastFeed.id
                )
            }
        val feedUrl = data.mapNotNull { it.podcastSource }
        val deferredChannels = feedUrl.map { async { it.getRssChannel(parseRssRepository) } }
        val rssChannels = deferredChannels.awaitAll()
        combine(
            rssChannels
        ) { results ->
            results.mapNotNull { it.data }
        }.runCatching {
            this.collect { rssChannels ->
                //TODO: tạm thời xoá hết episodes theo podcastID, rồi add lại list episodes mới sync về
                if (rssChannels.isNotEmpty()) {
                    data.map {
                        it.podcastId
                    }.also {
                        updatePodcastRepository.deleteEpisodes(it)
                    }
                }
                rssChannels.forEachIndexed { index, rssChannel ->
                    val episodes = rssChannel.items.distinctBy { it.guid }.map {
                        val actual: Date = DateUtils.parse(it.pubDate)
                        PodcastFeedItemEntity(
                            id = BigInteger(it.guid?.toByteArray()).toLong(),
                            idPodcast = data[index].podcastId,
                            songId = BigInteger(it.guid?.toByteArray()).toLong(),
                            title = it.title ?: "",
                            artist = HtmlCompat.fromHtml(
                                it.description ?: it.content.toString(),
                                HtmlCompat.FROM_HTML_MODE_COMPACT
                            ).toString(),
                            duration = if (it.itunesItemData?.duration != null)
                                inMillis(it.itunesItemData?.duration.toString())
                            else 0,
                            year = Instant.fromEpochMilliseconds(actual.time)
                                .toLocalDateTime(ZoneId.systemDefault().toKotlinTimeZone()).year,
                            dateModified = actual.time,
                            data = it.audio ?: "",
                            image = it.itunesItemData?.image,
                            publishDate = Instant.fromEpochMilliseconds(actual.time),
                            mimeType = MimeTypes.AUDIO_DTS_HD
                        )
                    }
                    updatePodcastRepository.insertPodcastFeedItem(episodes)

                    Timber.d("Sync Feed: ${rssChannel.title}")
                }
                analyticsHelper.logSyncFinished(true)
            }
        }.onFailure {
            Timber.e(it)
            analyticsHelper.logSyncFinished(false)
            Result.failure()
        }.onSuccess {
            Result.success()
        }
        Result.success()
    }

    companion object {
        const val WORK_TAG_FEED_UPDATE = "feedUpdate"

        /**
         * Expedited one time work to sync data on app startup
         */
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .setInputData(SyncWorker::class.delegatedData())
            .addTag(WORK_TAG_FEED_UPDATE)
            .build()
        fun nextSyncWork() = PeriodicWorkRequestBuilder<DelegatingWorker>(1, TimeUnit.DAYS)
            .setConstraints(SyncConstraints)
            .setInputData(SyncWorker::class.delegatedData())
            .addTag(WORK_TAG_FEED_UPDATE)
            .setNextScheduleTimeOverride(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(20))
            .build()
    }

    data class PodcastData(
        val podcastSource: String?,
        val podcastId: Long
    )
}
