package caios.android.kanade.core.common.network

import androidx.annotation.AnyThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import caios.android.kanade.core.common.network.di.SHOW_ADS
import com.applovin.sdk.AppLovinSdk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch

abstract class BaseViewModel<A : ViewModelAction>(
    @Dispatcher(KanadeDispatcher.Default)
    val defaultDispatcher: CoroutineDispatcher,
    appLoVinSdk: AppLovinSdk
) : ViewModel() {

    private val _action by lazy { MutableSharedFlow<A>() }
    val action: SharedFlow<A>
        get() = _action

    @AnyThread
    protected suspend fun emitAction(action: A) = defaultDispatcher {
        _action.emit(action)
    }

    private val _uiState = MutableStateFlow(false)
    private val loadAdState = _uiState.asSharedFlow()

    init {
        if (SHOW_ADS) {
            appLoVinSdk.initializeSdk {
                viewModelScope.launch {
                    _uiState.emit(true)
                }
            }.runCatching {
                viewModelScope.launch {
                    delay(3000)
                    if (!_uiState.value) {
                        _uiState.emit(true)
                    }
                }
            }
        }
    }
}

interface ViewModelAction

class NoneAction : ViewModelAction
