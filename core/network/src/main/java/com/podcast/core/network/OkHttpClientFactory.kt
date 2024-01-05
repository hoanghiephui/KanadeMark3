package com.podcast.core.network

import android.content.Context
import com.podcast.core.network.util.newBuilder
import io.ktor.http.headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient

internal object OkHttpClientFactory {

    fun create(
        applicationInterceptors: Set<Interceptor>,
        context: Context
    ): OkHttpClient =
        newBuilder(context)
            .apply {
                applicationInterceptors.forEach { addInterceptor(it) }
            }
            .build()
}
