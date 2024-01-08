package caios.android.kanade.core.common.network.util

import timber.log.Timber
import java.util.concurrent.TimeUnit

@Throws(NumberFormatException::class)
fun inMillis(durationStr: String): Long {
    Timber.d("TIME: $durationStr")
    val parts = durationStr.trim { it <= ' ' }.split(":".toRegex()).dropLastWhile { it.isEmpty() }
        .toTypedArray()
    return when (parts.size) {
        1 -> {
            toMillis(parts[0])
        }
        2 -> {
            toMillis("0", parts[0], parts[1])
        }
        3 -> {
            toMillis(parts[0], parts[1], parts[2])
        }
        else -> {
            0
        }
    }
}

private fun toMillis(hours: String, minutes: String, seconds: String): Long {
    return (TimeUnit.HOURS.toMillis(hours.toLong())
            + TimeUnit.MINUTES.toMillis(minutes.toLong())
            + toMillis(seconds))
}

private fun toMillis(seconds: String): Long {
    return if (seconds.contains(".")) {
        val value = seconds.toFloat()
        val millis = value % 1
        TimeUnit.SECONDS.toMillis(value.toLong()) + (millis * 1000).toLong()
    } else {
        TimeUnit.SECONDS.toMillis(seconds.toLong())
    }
}
