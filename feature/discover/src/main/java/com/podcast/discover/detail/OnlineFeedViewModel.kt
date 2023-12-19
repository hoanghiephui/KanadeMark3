package com.podcast.discover.detail

import android.net.Uri
import androidx.annotation.AnyThread
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.ScreenState
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Artwork
import caios.android.kanade.core.model.music.Song
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
import java.math.BigInteger
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
    private val errorsDispatcher: ErrorsDispatcher
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

    @AnyThread
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

    @AnyThread
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

    @AnyThread
    private fun createFeedUiState(
        item: RssChannel?,
        podcastRssResult: Result<RssChannel>?
    ): ScreenState<Artist> = when {
        item != null -> ScreenState.Idle(
            Artist(
                artist = item.title ?: "",
                artistId = 0,
                albums = item.items.map {
                    Album(
                        album = it.title ?: "",
                        albumId = BigInteger(it.guid?.toByteArray()).toLong(),
                        songs = listOf(
                            Song(
                                id = BigInteger(it.guid?.toByteArray()).toLong(),
                                title = it.title ?: "",
                                artistId = BigInteger(it.guid?.toByteArray()).toLong(),
                                artist = item.title ?: "",
                                album = "",
                                albumId = BigInteger(it.guid?.toByteArray()).toLong(),
                                duration = 10,
                                year = 0,
                                track = 0,
                                mimeType = "",
                                data = "",
                                dateModified = 0,
                                uri = Uri.EMPTY,
                                albumArtwork = Artwork.dummy(),
                                artistArtwork = Artwork.dummy()
                            )
                        ),
                        artwork = Artwork.Web(url = item.image?.url ?: "")
                    )
                },
                artwork = Artwork.Web(url = item.image?.url ?: ""),
                description = item.description
            )
        )

        podcastRssResult.isLoading -> ScreenState.Loading
        else -> ScreenState.Error(
            message = R.string.error_no_data,
            retryTitle = R.string.common_close
        )
    }


    companion object {
        const val FEED_URL = "feed_url"
    }
}
