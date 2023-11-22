package com.podcast.core.network.adapter

import com.podcast.core.network.StatusCode
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

internal object StatusCodeAdapter {

    @FromJson
    fun fromJson(code: Int): StatusCode =
        StatusCode.fromCode(code)

    @ToJson
    fun toJson(statusCode: StatusCode): Int =
        statusCode.code
}
