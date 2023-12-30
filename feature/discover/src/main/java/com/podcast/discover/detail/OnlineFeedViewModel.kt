package com.podcast.discover.detail

import android.content.Context
import android.net.Uri
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import caios.android.kanade.core.common.network.BaseViewModel
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.common.network.NoneAction
import caios.android.kanade.core.common.network.Result
import caios.android.kanade.core.common.network.asFlowResult
import caios.android.kanade.core.common.network.data
import caios.android.kanade.core.common.network.extension.safeCollect
import caios.android.kanade.core.common.network.isLoading
import caios.android.kanade.core.common.network.onResultError
import caios.android.kanade.core.common.network.util.DateUtils
import caios.android.kanade.core.common.network.util.inMillis
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.ScreenState
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Artwork
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.music.MusicController
import caios.android.kanade.core.repository.SongRepository
import caios.android.kanade.core.repository.podcast.FeedDiscoveryRepository
import caios.android.kanade.core.repository.podcast.ParseRssRepository
import caios.android.kanade.core.ui.error.ErrorsDispatcher
import com.podcast.core.network.util.PodcastDownloader
import com.prof18.rssparser.model.RssChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinTimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigInteger
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class OnlineFeedViewModel @Inject constructor(
    private val repository: ParseRssRepository,
    private val feedRepository: FeedDiscoveryRepository,
    @Dispatcher(KanadeDispatcher.IO)
    private val ioDispatcher: CoroutineDispatcher,
    @Dispatcher(KanadeDispatcher.Default)
    defaultDispatcher: CoroutineDispatcher,
    private val errorsDispatcher: ErrorsDispatcher,
    private val musicController: MusicController,
    private val songRepository: SongRepository,
    val download: PodcastDownloader
) : BaseViewModel<NoneAction>(defaultDispatcher) {
    private var imId: String? = null
    private val feedState = MutableStateFlow<RssChannel?>(null)
    private val fetchNewFeedResultState = MutableStateFlow<Result<RssChannel>?>(null)
    val screenState =
        combine(
            feedState,
            fetchNewFeedResultState
        ) { item, podcastResponseResult ->
            createFeedUiState(
                item = item,
                podcastRssResult = podcastResponseResult,
                imId = imId
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ScreenState.Loading,
        )

    fun getLookFeed(feedId: String) {
        this.imId = feedId
        viewModelScope.launch(defaultDispatcher) {
            val currentState = fetchNewFeedResultState.getAndUpdate { Result.Loading() }
            if (currentState !is Result.Loading) {
                asFlowResult {
                    feedRepository.getLookFeed(feedId)
                }.safeCollect(
                    onEach = {
                        val feedUrlName = it.data?.results?.first()?.feedUrl ?: return@safeCollect
                        fetchRssDetail(feedUrlName)
                    },
                    onError = errorsDispatcher::dispatch
                )
            }
        }
    }

    private suspend fun fetchRssDetail(feedUrl: String) {
        asFlowResult {
            repository.getRssDetail(feedUrl)
        }.onResultError(errorsDispatcher::dispatch).safeCollect(
            onEach = { result ->
                feedState.update { result.data }
                fetchNewFeedResultState.emit(result)
            },
            onError = errorsDispatcher::dispatch,
        )
    }

    private suspend fun createFeedUiState(
        item: RssChannel?,
        podcastRssResult: Result<RssChannel>?,
        imId: String?
    ): ScreenState<Artist> = when {

        item != null -> withContext(ioDispatcher) {
            val artistId = BigInteger(imId?.toByteArray()).toLong()
            val isSubscribe = feedRepository.isSubscribe(artistId)
            ScreenState.Idle(
                Artist(
                    isSubscribe = isSubscribe,
                    artist = item.title ?: "",
                    artistId = artistId,
                    albums = item.items.map {
                        val actual: Date = DateUtils.parse(it.pubDate)
                        val id = BigInteger(it.guid?.toByteArray()).toLong()
                        val localPodcast = feedRepository.loadPodcast(id)
                        val song = Song(
                            id = id,
                            title = it.title ?: "",
                            artistId = artistId,
                            artist = HtmlCompat.fromHtml(
                                it.description.toString(),
                                HtmlCompat.FROM_HTML_MODE_COMPACT
                            ).toString(),
                            album = "",
                            albumId = artistId,
                            duration = inMillis(it.itunesItemData?.duration.toString()),
                            year = Instant.fromEpochMilliseconds(actual.time)
                                .toLocalDateTime(ZoneId.systemDefault().toKotlinTimeZone()).year,
                            track = 1,
                            mimeType = MimeTypes.AUDIO_MPEG,
                            data = it.audio ?: "",
                            dateModified = actual.time,
                            uri = Uri.parse(it.audio ?: ""),
                            albumArtwork = if (it.itunesItemData?.image != null) Artwork.Web(url = it.itunesItemData?.image.toString()) else Artwork.dummy(
                                it.title ?: "PO"
                            ),
                            artistArtwork = if (it.itunesItemData?.image != null) Artwork.Web(url = it.itunesItemData?.image.toString()) else Artwork.dummy(
                                it.title ?: "PO"
                            ),
                            isStream = true,
                            isDownloaded = localPodcast != null,
                            publishDate = Instant.fromEpochMilliseconds(actual.time),
                            urlImage = it.itunesItemData?.image
                        )
                        Album(
                            album = it.title ?: "",
                            albumId = artistId,
                            songs = listOf(song),
                            artwork = if (item.image?.url != null) Artwork.Web(url = item.image?.url.toString()) else Artwork.dummy(
                                it.title ?: "PO"
                            )
                        )
                    },
                    artwork = if (item.image?.url != null) Artwork.Web(url = item.image?.url.toString()) else Artwork.dummy(
                        item.title ?: "PO"
                    ),
                    description = HtmlCompat.fromHtml(
                        item.description.toString(),
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    ).toString(),
                    author = item.itunesChannelData?.author,
                    urlAvatar = item.image?.url
                )
            )
        }

        podcastRssResult.isLoading -> ScreenState.Loading
        else -> ScreenState.Error(
            message = R.string.error_no_data,
            retryTitle = R.string.common_close
        )
    }

    fun onNewPlay(songs: List<Song>, index: Int) {
        musicController.playerEvent(
            PlayerEvent.PreviewPlay(
                index = index,
                queue = songs[index],
                playWhenReady = true,
            ),
        )
    }

    fun onAddToQuote(song: Song) {
        musicController.addToQueue(song)
    }

    fun playerEvent(event: PlayerEvent) {
        musicController.playerEvent(event)
    }


    fun downloadPodcast(
        song: Song,
        permission: Boolean,
        context: Context,
        downloadProgressListener: (Float, Int) -> Unit
    ): String {
        return if (permission) {
            download.downloadBook(song = song,
                downloadProgressListener = downloadProgressListener,
                onDownloadSuccess = {
                    insertIntoDB(song, download.getMediaFileName(song))
                })
            context.getString(R.string.downloading_podcast)
        } else {
            context.getString(R.string.storage_perm_error)
        }
    }

    fun onSubscribePodcast(
        imId: String,
        artist: Artist
    ) {
        viewModelScope.launch(defaultDispatcher) {
            feedRepository.subscribePodcast(imId, artist)
        }
    }

    private fun insertIntoDB(song: Song, filename: String) =
        viewModelScope.launch(defaultDispatcher) {
            feedRepository.savePodcast(song, "${PodcastDownloader.FILE_FOLDER_PATH}/$filename")
        }

    fun onUnSubscribePodcast(imId: Long) {
        viewModelScope.launch(defaultDispatcher) {
            feedRepository.onUnSubscribePodcast(imId)
        }
    }
}
