package caios.android.kanade.core.ui.error

import android.content.Context
import caios.android.kanade.core.model.exception.NoNetworkException
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import caios.android.kanade.core.design.R as commonR

class CommonErrorMapper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun map(e: Throwable): Error =
        when (e) {
            is NoNetworkException ->
                SimpleMessageError(context.getString(commonR.string.common_error_no_network))
            else -> {
                Timber.e(e)
                SimpleMessageError(context.getString(commonR.string.common_error_unexpected))
            }
        }
}
