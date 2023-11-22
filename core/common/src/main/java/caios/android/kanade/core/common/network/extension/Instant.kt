package caios.android.kanade.core.common.network.extension

import java.time.Instant

fun Instant?.isExpired(now: Instant, timeout: Long): Boolean =
    this?.toEpochMilli()?.let { now.toEpochMilli() - it > timeout } ?: true
