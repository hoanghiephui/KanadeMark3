package caios.android.kanade.core.common.network

import java.time.Instant
import javax.inject.Inject

interface DateTimeProvider {
    fun now(): Instant
}

class DefaultDateTimeProvider @Inject constructor() : DateTimeProvider {

    override fun now(): Instant = Instant.now()
}
