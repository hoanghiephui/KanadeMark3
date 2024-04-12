package caios.android.kanade.core.design

import androidx.annotation.AnyThread
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.invoke

abstract class BaseViewModel<A : ViewModelAction>(
    val defaultDispatcher: CoroutineDispatcher,
    appLoVinSdk: AppLovinSdk,
    appLovinSdkInitialization: AppLovinSdkInitializationConfiguration
) : BaseAdsViewModel(appLoVinSdk, appLovinSdkInitialization) {
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
