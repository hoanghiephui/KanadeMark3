package caios.android.kanade.core.repository.podcast

import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.common.network.Result
import caios.android.kanade.core.common.network.asFlowResult
import com.prof18.rssparser.RssParser
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ParseRssRepository {
    suspend fun getRssDetail(
        feedUrl: String
    ): RssChannel
}

class ParseRssRepositoryImpl @Inject constructor(
    private val rssParserBuilder: RssParser,
    @Dispatcher(KanadeDispatcher.IO) private val dispatcher: CoroutineDispatcher,
) : ParseRssRepository {
    override suspend fun getRssDetail(feedUrl: String): RssChannel = withContext(dispatcher) {
        rssParserBuilder.getRssChannel(feedUrl)
    }
}

fun String.getRssChannel(
    parseRssRepository: ParseRssRepository
): Flow<Result<RssChannel>> = asFlowResult {
    parseRssRepository.getRssDetail(this)
}
