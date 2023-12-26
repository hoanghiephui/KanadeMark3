package com.podcast.discover.detail

import android.net.Uri
import androidx.annotation.AnyThread
import androidx.compose.ui.util.fastMap
import androidx.core.text.HtmlCompat
import androidx.lifecycle.SavedStateHandle
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
import timber.log.Timber
import java.math.BigInteger
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class OnlineFeedViewModel @Inject constructor(
    private val repository: ParseRssRepository,
    private val feedRepository: FeedDiscoveryRepository,
    val savedStateHandle: SavedStateHandle,
    @Dispatcher(KanadeDispatcher.IO)
    private val ioDispatcher: CoroutineDispatcher,
    @Dispatcher(KanadeDispatcher.Default)
    defaultDispatcher: CoroutineDispatcher,
    private val errorsDispatcher: ErrorsDispatcher,
    private val musicController: MusicController,
    private val songRepository: SongRepository,
) : BaseViewModel<NoneAction>(defaultDispatcher) {

    private val feedState = MutableStateFlow<RssChannel?>(null)
    private val fetchNewFeedResultState = MutableStateFlow<Result<RssChannel>?>(null)
    val screenState =
        combine(
            feedState,
            fetchNewFeedResultState
        ) { item, podcastResponseResult ->
            createFeedUiState(
                item = item,
                podcastRssResult = podcastResponseResult
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ScreenState.Loading,
        )

    fun getLookFeed(feedId: String) {
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
        podcastRssResult: Result<RssChannel>?
    ): ScreenState<Artist> = when {

        item != null -> withContext(ioDispatcher) {
            ScreenState.Idle(
                Artist(
                    artist = item.title ?: "",
                    artistId = 0,
                    albums = item.items.map {
                        val actual: Date = DateUtils.parse(it.pubDate)
                        val song = Song(
                            id = BigInteger(it.guid?.toByteArray()).toLong(),
                            title = it.title ?: "",
                            artistId = BigInteger(it.guid?.toByteArray()).toLong(),
                            artist = HtmlCompat.fromHtml(it.description.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT).toString(),
                            album = "",
                            albumId = BigInteger(it.guid?.toByteArray()).toLong(),
                            duration = inMillis(it.itunesItemData?.duration.toString()),
                            year = actual.year,
                            track = 1,
                            mimeType = MimeTypes.AUDIO_DTS_HD,
                            data = it.audio ?: "",
                            dateModified = actual.time,
                            uri = Uri.parse(it.audio ?: ""),
                            albumArtwork = if (it.itunesItemData?.image != null) Artwork.Web(url = it.itunesItemData?.image.toString()) else Artwork.dummy(),
                            artistArtwork = Artwork.Web(url = item.image?.url.toString()),
                            isStream = true
                        )
                        //songRepository.songsPodcast(song)
                        Album(
                            album = it.title ?: "",
                            albumId = BigInteger(it.guid?.toByteArray()).toLong(),
                            songs = listOf(song),
                            artwork = if (item.image?.url != null) Artwork.Web(url = item.image?.url.toString()) else Artwork.dummy()
                        )
                    },
                    artwork = if (item.image?.url != null) Artwork.Web(url = item.image?.url.toString()) else Artwork.dummy(),
                    description = HtmlCompat.fromHtml(item.description.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT).toString(),
                    author = item.itunesChannelData?.author
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

    fun onDownloadSong(song: Song) {

    }

    fun playerEvent(event: PlayerEvent) {
        musicController.playerEvent(event)
    }


    companion object {
        const val FEED_URL = "feed_url"
    }
}
