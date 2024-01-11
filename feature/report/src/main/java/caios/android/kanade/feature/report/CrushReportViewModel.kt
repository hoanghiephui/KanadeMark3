package caios.android.kanade.feature.report

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import caios.android.kanade.core.common.network.di.EVENT_RESTART_APP
import caios.android.kanade.core.model.ScreenState
import caios.android.kanade.core.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class CrushReportViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    private val eventBus: MutableSharedFlow<Int>
) : ViewModel() {

    val screenState = userDataRepository.userData.map {
        ScreenState.Idle(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    fun onRestartApp() {
        viewModelScope.launch {
            eventBus.emit(EVENT_RESTART_APP)
        }
    }
}
