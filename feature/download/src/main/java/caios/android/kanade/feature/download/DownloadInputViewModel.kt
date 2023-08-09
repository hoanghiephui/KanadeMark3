package caios.android.kanade.feature.download

import android.util.Patterns
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.State
import caios.android.kanade.core.model.download.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DownloadInputViewModel @Inject constructor(
    @Dispatcher(KanadeDispatcher.IO) private val io: CoroutineDispatcher,
) : ViewModel() {

    var uiState by mutableStateOf(DownloadInputUiState())
        private set

    private val formatter = Json { ignoreUnknownKeys = true }

    fun updateUrl(input: String) {
        uiState = uiState.copy(
            url = input,
            error = if (Patterns.WEB_URL.matcher(input).matches() || input.isEmpty()) null else R.string.download_input_error_invalid_url,
        )
    }

    fun fetchInfo() {
        viewModelScope.launch {
            uiState = uiState.copy(state = State.Loading)
            uiState = fetchVideoInfo(uiState.url).fold(
                onSuccess = {
                    uiState.copy(
                        state = State.Idle,
                        error = null,
                    )
                },
                onFailure = {
                    uiState.copy(
                        state = State.Error,
                        error = R.string.download_input_error_failed,
                    )
                }
            )
        }
    }

    private suspend fun fetchVideoInfo(url: String): Result<VideoInfo> {
        return YoutubeDLRequest(url).apply {
            addOption("-x")
            addOption("-R", "1")
            addOption("--playlist-items", "1")
            addOption("--socket-timeout", "5")
        }.run {
            getVideoInfo(this)
        }
    }

    private suspend fun getVideoInfo(request: YoutubeDLRequest): Result<VideoInfo> = withContext(io){
        request.addOption("--dump-json")

        kotlin.runCatching {
            val response = YoutubeDL.getInstance().execute(request)
            val info = formatter.decodeFromString<VideoInfo>(response.out)

            Timber.d("getVideoInfo: success for getting video info. [${info.title}]")

            return@runCatching info
        }
    }
}

@Stable
data class DownloadInputUiState(
    val state: State = State.Idle,
    val url: String = "",
    val error: Int? = null,
)
