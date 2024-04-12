package caios.android.kanade

import android.app.Application
import android.content.Intent
import android.os.Build
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeConfig
import caios.android.kanade.core.common.network.KanadeDebugTree
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.common.network.di.EVENT_RESTART_APP
import caios.android.kanade.feature.report.CrushReportActivity
import caios.android.kanade.workers.Sync
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.android.material.color.DynamicColors
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class KanadeApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var kanadeConfig: KanadeConfig

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    @Inject
    @Dispatcher(KanadeDispatcher.IO)
    lateinit var io: CoroutineDispatcher

    @Inject
    lateinit var imageLoader: Provider<ImageLoader>

    @Inject
    lateinit var evenBus: MutableSharedFlow<Int>


    private val scope by lazy {
        CoroutineScope(Dispatchers.Default)
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(KanadeDebugTree())

        DynamicColors.applyToActivitiesIfAvailable(this)

        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            startCrushReportActivity(e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }

        scope.launch {
            evenBus.collectLatest {
                if (it == EVENT_RESTART_APP) {
                    restartApp()
                }
            }
        }
        Sync.initialize(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        scope.cancel()
    }

    override fun newImageLoader(): ImageLoader =
        imageLoader.get()

    private fun startCrushReportActivity(e: Throwable) {
        Timber.e(e)

        startActivity(
            Intent(this, CrushReportActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("report", getVersionReport() + "\n" + e.stackTraceToString())
            },
        )
    }

    private fun restartApp() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
    }

    private fun getVersionReport() = buildString {
        val release =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Build.VERSION.RELEASE_OR_CODENAME else Build.VERSION.RELEASE

        appendLine("Version: ${kanadeConfig.versionName} (${kanadeConfig.versionCode})")
        appendLine("Device Information: $release (${Build.VERSION.SDK_INT})")
        appendLine("Device Model: ${Build.MODEL} (${Build.MANUFACTURER})")
        appendLine("Supported ABIs: ${Build.SUPPORTED_ABIS.contentToString()}")
    }
}
