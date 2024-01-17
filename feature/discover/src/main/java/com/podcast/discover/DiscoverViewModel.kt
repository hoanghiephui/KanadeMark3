package com.podcast.discover

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import caios.android.kanade.core.design.BaseViewModel
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.design.NoneAction
import caios.android.kanade.core.common.network.Result
import caios.android.kanade.core.common.network.asFlowResult
import caios.android.kanade.core.common.network.data
import caios.android.kanade.core.common.network.extension.safeCollect
import caios.android.kanade.core.common.network.onResultError
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.ScreenState
import caios.android.kanade.core.model.podcast.Advanced
import caios.android.kanade.core.model.podcast.EntryItem
import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import caios.android.kanade.core.repository.UserDataRepository
import caios.android.kanade.core.repository.podcast.FeedDiscoveryRepository
import caios.android.kanade.core.ui.error.ErrorsDispatcher
import com.applovin.sdk.AppLovinSdk
import com.podcast.core.network.api.Genres
import com.podcast.core.usecase.ItunesFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale.getDefault
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    @Dispatcher(KanadeDispatcher.IO)
    private val ioDispatcher: CoroutineDispatcher,
    @Dispatcher(KanadeDispatcher.Default)
    defaultDispatcher: CoroutineDispatcher,
    private val errorsDispatcher: ErrorsDispatcher,
    private val feedDiscoveryUseCase: ItunesFeedUseCase,
    private val feedRepository: FeedDiscoveryRepository,
    private val userDataRepository: UserDataRepository,
    private val savedStateHandle: SavedStateHandle,
    appLoVinSdk: AppLovinSdk
) : BaseViewModel<NoneAction>(defaultDispatcher, appLoVinSdk) {
    private val genres: Genres
        get() = Genres.fromInt(
            savedStateHandle[DiscoverMoreData_Genres] ?: Genres.TOP.id
        )

    private val itemsAdvanced = mutableListOf(
        Advanced(1, R.drawable.baseline_search_24, "Search Apple Podcasts"),
        Advanced(2, R.drawable.baseline_search_24, "Search fyyd"),
        Advanced(3, R.drawable.baseline_search_24, "Search Podcast Index")
    )

    val uiMoreState =
        combineTransform(
            userDataRepository.userData,
            feedRepository.loadSubscribe()
                .map { podcastModels -> podcastModels.map { it.podcastFeed.id } }
        ) { userData, subscribedFeeds ->
            val countryCode = userData.countryCode.ifBlank { getDefault().country }
            selectCountryCode = countryCode
            countryCode.getTopPodcastByGenres(genres).map {
                withContext(ioDispatcher) {
                    val topFeedResult =
                        it.data?.feed?.entry?.toMap(subscribedFeeds) ?: emptyList()
                    ScreenState.Idle(topFeedResult.toImmutableList())
                }
            }.safeCollect(
                onEach = {
                    emit(it)
                },
                onError = errorsDispatcher::dispatch
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ScreenState.Loading,
        )

    val uiState =
        combineTransform(
            userDataRepository.userData,
            feedRepository.loadSubscribe()
                .map { podcastModels -> podcastModels.map { it.podcastFeed.id } }
        ) { userData, subscribedFeeds ->
            val countryCode = userData.countryCode.ifBlank { getDefault().country }
            selectCountryCode = countryCode
            val genres =
                listOf(Genres.TOP, Genres.HEALTH, Genres.EDUCATION, Genres.MUSIC, Genres.SOCIETY)
            combine(
                genres.map {
                    countryCode.getTopPodcastByGenres(it).map { result ->
                        result.data?.feed?.entry
                    }
                }
            ) { results ->
                withContext(ioDispatcher) {
                    val topFeedResult =
                        results[0].toMap(subscribedFeeds)
                    val healthFeedResult =
                        results[1].toMap(subscribedFeeds)
                    val educationResult =
                        results[2].toMap(subscribedFeeds)
                    val musicResult =
                        results[3].toMap(subscribedFeeds)
                    val societyResult =
                        results[4].toMap(subscribedFeeds)
                    ScreenState.Idle(
                        Discover(
                            topFeedResult.toImmutableList(),
                            healthFeedResult.toImmutableList(),
                            educationResult.toImmutableList(),
                            musicResult.toImmutableList(),
                            societyResult.toImmutableList(),
                            itemsAdvanced.toImmutableList()
                        )
                    )
                }

            }.collect {
                emit(it)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ScreenState.Loading,
        )

    var selectCountryCode: String = getDefault().country
        set(value) {
            field = value
        }

    private fun String.getTopPodcastByGenres(
        genres: Genres
    ): Flow<Result<ItunesTopPodcastResponse>> =
        asFlowResult {
            feedDiscoveryUseCase.getTopPodcastByGenres(
                this,
                25,
                genres = genres
            )
        }.onResultError(errorsDispatcher::dispatch)


    fun saveCountryCode(countryCode: String) {
        viewModelScope.launch(defaultDispatcher) {
            userDataRepository.setCountryCode(countryCode)
        }
    }

    private fun List<EntryItem>?.toMap(subscribedFeeds: List<Long>) =
        this?.filterNot { subscribedFeeds.contains(it.id?.attributes?.imId?.toLong()) }
            ?: emptyList()
}
