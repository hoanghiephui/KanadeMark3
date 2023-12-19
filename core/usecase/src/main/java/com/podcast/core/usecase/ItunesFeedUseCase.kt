package com.podcast.core.usecase

import androidx.annotation.AnyThread
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.common.network.asFlowResult
import caios.android.kanade.core.common.network.data
import caios.android.kanade.core.common.network.extension.safeCollect
import caios.android.kanade.core.common.network.onResultError
import caios.android.kanade.core.model.podcast.FeedData
import caios.android.kanade.core.repository.podcast.FeedDiscoveryRepository
import caios.android.kanade.core.repository.podcast.ParseRssRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ItunesFeedUseCase @Inject constructor(
    private val repository: FeedDiscoveryRepository,
    private val parseRssRepository: ParseRssRepository,
    @Dispatcher(KanadeDispatcher.IO)
    private val ioDispatcher: CoroutineDispatcher,
) {
    @AnyThread
    suspend fun getTopPodcast(
        country: String,
        limit: Int
    ) = repository.getTopPodcast(country, limit)

    /*@AnyThread
    suspend fun getLookFeed(id: String): Flow<FeedData> {
        withContext(ioDispatcher) {
            val lookFeed = async { repository.getLookFeed(id) }.await()
            if (lookFeed.results != null) {
                parseRssRepository.getRssDetail(lookFeed.results?.first()?.feedUrl!!)
            }

        }
    }*/
}
