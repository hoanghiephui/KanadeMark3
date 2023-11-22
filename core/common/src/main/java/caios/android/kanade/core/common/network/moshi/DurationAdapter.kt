package caios.android.kanade.core.common.network.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Duration

internal object DurationAdapter {

    @ToJson
    fun toJson(duration: Duration): String = duration.toMinutes().toString()

    @FromJson
    fun fromJson(json: Long): Duration = Duration.ofMinutes(json)
}
