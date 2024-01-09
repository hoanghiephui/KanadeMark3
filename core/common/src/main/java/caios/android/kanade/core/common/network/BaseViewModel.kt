package caios.android.kanade.core.common.network

import androidx.annotation.AnyThread
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.invoke

abstract class BaseViewModel<A : ViewModelAction>(
    @Dispatcher(KanadeDispatcher.Default)
    val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _action by lazy { MutableSharedFlow<A>() }
    val action: SharedFlow<A>
        get() = _action

    @AnyThread
    protected suspend fun emitAction(action: A) = defaultDispatcher {
        _action.emit(action)
    }
}

interface ViewModelAction

class NoneAction : ViewModelAction