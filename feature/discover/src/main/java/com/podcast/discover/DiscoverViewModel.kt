package com.podcast.discover

import androidx.annotation.AnyThread
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
import caios.android.kanade.core.model.podcast.Advanced
import caios.android.kanade.core.model.podcast.EntryItem
import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import caios.android.kanade.core.repository.UserDataRepository
import caios.android.kanade.core.repository.podcast.FeedDiscoveryRepository
import caios.android.kanade.core.ui.error.ErrorsDispatcher
import com.podcast.core.usecase.ItunesFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Locale.*
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    @Dispatcher(KanadeDispatcher.IO)
    private val ioDispatcher: CoroutineDispatcher,
    @Dispatcher(KanadeDispatcher.Default)
    defaultDispatcher: CoroutineDispatcher,
    private val errorsDispatcher: ErrorsDispatcher,
    private val movieItemMapper: DiscoverFeedItemMapper,
    private val feedDiscoveryUseCase: ItunesFeedUseCase,
    private val feedRepository: FeedDiscoveryRepository,
    private val userDataRepository: UserDataRepository
) : BaseViewModel<NoneAction>(defaultDispatcher) {
    private val feedState = MutableStateFlow(emptyList<EntryItem>())
    private val fetchNewFeedResultState = MutableStateFlow<Result<ItunesTopPodcastResponse>?>(null)
    val uiState =
        combine(
            feedState.map(movieItemMapper::map),
            fetchNewFeedResultState,
            feedRepository.loadSubscribe()
                .map { podcastModels -> podcastModels.map { it.podcastFeed.id } }
        ) { items, podcastResponseResult, subscribedFeeds ->
            createDiscoverUiState(
                items = items,
                podcastResponseResult = podcastResponseResult,
                subscribedFeeds = subscribedFeeds
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ScreenState.Loading,
        )

    val itemsAdvanced = mutableListOf(
        Advanced(1, R.drawable.baseline_search_24, "Search Apple Podcasts"),
        Advanced(2, R.drawable.baseline_search_24, "Search fyyd"),
        Advanced(3, R.drawable.baseline_search_24, "Search Podcast Index")
    )

    val countryCode = userDataRepository.userData.map {
        ScreenState.Idle(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    var selectCountryCode = getDefault().country
        set(value) {
            field = value
        }

    init {
        viewModelScope.launch(defaultDispatcher) {
            countryCode.collect {
                if (it is ScreenState.Idle) {
                    val countryCode = it.data.countryCode.ifBlank { getDefault().country }
                    fetchNewDiscoverPodcast(countryCode)
                    selectCountryCode = countryCode
                }
            }
        }
    }

    @AnyThread
    private suspend fun createDiscoverUiState(
        items: ImmutableList<EntryItem>,
        podcastResponseResult: Result<ItunesTopPodcastResponse>?,
        subscribedFeeds: List<Long>
    ): ScreenState<Discover> = when {
        items.isNotEmpty() -> withContext(ioDispatcher) {
            val suggestedPodcastsResult =
                items.filterNot { subscribedFeeds.contains(it.id?.attributes?.imId?.toLong()) }
            ScreenState.Idle(
                Discover(items = suggestedPodcastsResult.toImmutableList())
            )
        }

        podcastResponseResult.isLoading -> ScreenState.Loading
        else -> ScreenState.Error(
            message = R.string.error_no_data,
            retryTitle = R.string.common_close
        )
    }

    @AnyThread
    fun fetchNewDiscoverPodcast(countryCode: String) {
        viewModelScope.launch(defaultDispatcher) {

            val currentState = fetchNewFeedResultState.getAndUpdate { Result.Loading() }
            if (currentState !is Result.Loading) {
                asFlowResult {
                    feedDiscoveryUseCase.getTopPodcast(countryCode, 25)
                }.onResultError(errorsDispatcher::dispatch)
                    .safeCollect(
                        onEach = { result ->
                            val suggestedPodcasts = result.data?.feed?.entry
                            feedState.update { suggestedPodcasts.orEmpty() }
                            fetchNewFeedResultState.emit(result)
                        },
                        onError = errorsDispatcher::dispatch,
                    )
            }
        }
    }

    fun saveCountryCode(countryCode: String) {
        viewModelScope.launch(defaultDispatcher) {
            userDataRepository.setCountryCode(countryCode)
        }
    }
}