package caios.android.kanade.core.repository.podcast

import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.model.podcast.IndexResponse
import com.podcast.core.network.datasource.FyyDDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface IndexRepository {
    suspend fun searchPodcast(
        query: String
    ): IndexResponse
}

class IndexRepositoryImpl @Inject constructor(
    private val networkDataSource: FyyDDataSource,
    @Dispatcher(KanadeDispatcher.IO)
    private val dispatcher: CoroutineDispatcher,
) : IndexRepository {

    override suspend fun searchPodcast(query: String): IndexResponse =
        withContext(dispatcher) {
            networkDataSource.searchPodcasts(query)
        }
}
