package caios.android.kanade.core.design

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import caios.android.kanade.core.design.component.AdViewState
import caios.android.kanade.core.design.component.MaxTemplateNativeAdViewComposableLoader
import com.applovin.sdk.AppLovinSdk
import timber.log.Timber

abstract class BaseAdsViewModel(
    private val appLoVinSdk: AppLovinSdk
): ViewModel() {
    private val callbacks = mutableStateListOf<String>()

    private val nativeAdLoader: MaxTemplateNativeAdViewComposableLoader by lazy {
        MaxTemplateNativeAdViewComposableLoader(this)
    }
    val adState: State<AdViewState> get() = nativeAdLoader.nativeAdView

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
        if (SHOW_ADS) {
            appLoVinSdk.initializeSdk {
                nativeAdLoader.loadAd(context, adUnitIdentifier)
                Timber.tag("Applovin").d("Load Ads")
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        nativeAdLoader.destroy()
    }
}

const val SHOW_ADS = true
