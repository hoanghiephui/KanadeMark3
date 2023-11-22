package caios.android.kanade.core.common.network.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant

internal object InstantAdapter {

    @FromJson
    fun fromJson(value: Long): Instant =
        Instant.ofEpochMilli(value)

    @ToJson
    fun toJson(instant: Instant): Long =
        instant.toEpochMilli()
}
