package caios.android.kanade.core.repository.podcast

import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.model.podcast.FyydResponse
import com.podcast.core.network.datasource.FyyDDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface FyyDRepository {
    suspend fun searchPodcast(
        query: String,
        limit: Int = 10
    ): FyydResponse
}

class FyyDRepositoryImpl @Inject constructor(
    private val networkDataSource: FyyDDataSource,
    @Dispatcher(KanadeDispatcher.IO)
    private val dispatcher: CoroutineDispatcher,
) : FyyDRepository {

    override suspend fun searchPodcast(query: String, limit: Int): FyydResponse =
        withContext(dispatcher) {
            networkDataSource.searchPodcast(query, limit)
        }
}
