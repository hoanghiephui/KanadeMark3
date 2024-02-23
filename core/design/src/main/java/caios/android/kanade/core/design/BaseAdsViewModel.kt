package caios.android.kanade.core.design

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import caios.android.kanade.core.design.component.AdViewState
import caios.android.kanade.core.design.component.MaxTemplateNativeAdViewComposableLoader
import com.applovin.sdk.AppLovinSdk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseAdsViewModel(
    appLoVinSdk: AppLovinSdk
): ViewModel() {
    private val callbacks = mutableStateListOf<String>()
    private val _uiState = MutableStateFlow(false)
    private val loadAdState = _uiState.asSharedFlow()
    private val nativeAdLoader: MaxTemplateNativeAdViewComposableLoader by lazy {
        MaxTemplateNativeAdViewComposableLoader(this)
    }
    val adState: State<AdViewState> get() = nativeAdLoader.nativeAdView

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

    /**
     * Log ad callbacks in the LazyColumn.
     * Uses the name of the function that calls this one in the log.
     */
    fun logCallback() {
        val callbackName = Throwable().stackTrace[1].methodName
        callbacks.add(callbackName)
        Timber.tag("Applovin").d(callbackName)
    }

    fun loadAds(
        context: Context,
        adUnitIdentifier: String
    ) {
        // Initialize ad with ad loader.
        viewModelScope.launch {
            loadAdState.collect {
                if (it && SHOW_ADS) {
                    nativeAdLoader.loadAd(context, adUnitIdentifier)
                    Timber.tag("Applovin").d("Load Ads")
                }
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        nativeAdLoader.destroy()
    }
}

const val SHOW_ADS = true
